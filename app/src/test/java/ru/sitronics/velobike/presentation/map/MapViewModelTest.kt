package ru.sitronics.velobike.presentation.map

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.domain.auth.AuthManager
import ru.sitronics.velobike.domain.chat.ChatManager
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.MapContentRepository
import ru.sitronics.velobike.domain.map.MapContentUseCase
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.domain.profile.ProfileUseCase
import ru.sitronics.velobike.domain.rent.MainRentStatus
import ru.sitronics.velobike.domain.rent.Rent
import ru.sitronics.velobike.domain.rent.RentData
import ru.sitronics.velobike.domain.rent.RentRepository
import ru.sitronics.velobike.domain.rent.RentUseCase
import ru.sitronics.velobike.domain.rent.RentUseCaseImp

@RunWith(MockitoJUnitRunner::class)
class MapTapTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private val context = mock<Context>()
    @Mock
    private val mapUC = mock<MapContentUseCase>()
    @Mock
    private val rentUC = mock<RentUseCase>()
    @Mock
    private val chatManager = mock<ChatManager>()

    private lateinit var mapVM: MapViewModel

    @Before
    fun setUp() {
        mapVM = MapViewModel(rentUC, mapUC, chatManager, context)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun bikeTap() = runTest {
        val intent = MapIntent.MapObjectTap(MarkerUserData.Bike("1"))

        mapUC.stub {
            on { getBike(any()) } doReturn Bike.empty()
        }

        mapVM.handleIntent(intent)
        // it needs here because of delay in _mapUiState change (see initStates())
        advanceUntilIdle()

        assertTrue(mapVM.mapUiState.value is MapUiState.BikeDetail)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun stationTap() = runTest {
        val intent = MapIntent.MapObjectTap(MarkerUserData.Station("1"))

        mapUC.stub {
            on { getStation(any()) } doReturn Parking.empty()
        }

        mapVM.handleIntent(intent)
        // it needs here because of delay in _mapUiState change (see initStates())
        advanceUntilIdle()

        assertTrue(mapVM.mapUiState.value is MapUiState.StationDetail)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun parkingTap() = runTest {
        val intent = MapIntent.MapObjectTap(MarkerUserData.Parking("1"))

        mapUC.stub {
            on { getParking(any()) } doReturn Parking.empty()
        }

        mapVM.handleIntent(intent)
        // it needs here because of delay in _mapUiState change (see initStates())
        advanceUntilIdle()

        assertTrue(mapVM.mapUiState.value is MapUiState.ParkingDetail)
    }
}

@RunWith(MockitoJUnitRunner::class)
class MapHandleActiveRentTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private val context = mock<Context>()
    @Mock
    private val mapUC = mock<MapContentUseCase>()
    @Mock
    private val chatManager = mock<ChatManager>()
    @Mock
    private val rentRepository = mock<RentRepository>()

    private lateinit var rentUC: RentUseCaseImp
    private lateinit var mapVM: MapViewModel

    @Before
    fun setUp() {
        val mapContentRepository = mock<MapContentRepository>()
        val profileUseCase = mock<ProfileUseCase>()
        val authManager = mock<AuthManager>()
        rentUC = RentUseCaseImp(rentRepository, mapContentRepository, profileUseCase, authManager, context)
        mapVM = MapViewModel(rentUC, mapUC, chatManager, context)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun activeRentShow() = runTest {
        val rent = Rent.empty().copy(rentStatus = MainRentStatus.IN_PROGRESS)

        rentRepository.stub {
            onBlocking { checkActiveRent() } doReturn Result.Success(listOf(rent))
        }
        rentRepository.stub {
            on { getData() } doReturn RentData(Bike.empty())
        }

        mapVM.handleIntent(MapIntent.MapStart)
        advanceTimeBy(1000)
        mapVM.handleIntent(MapIntent.MapStop)
        advanceUntilIdle()

        val states = mapVM.mapUiStates.take(HANDLE_ACTIVE_RENT_STATES_NUM).toList()

        assertEquals(true, (states[0] as MapUiState.ActiveRent).show)
        assertEquals(false, (states[1] as MapUiState.FinishingRent).show)
        assertEquals(false, (states[2] as MapUiState.ActiveRentBar).show)
        assertEquals(false, (states[3] as MapUiState.QrScanButton).show)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun activeRentBarShow() = runTest {
        val rent = Rent.empty()

        rentRepository.stub {
            onBlocking { checkActiveRent() } doReturn Result.Success(listOf(rent))
        }
        rentRepository.stub {
            on { getData() } doReturn RentData(Bike.empty())
        }

        mapVM.handleIntent(MapIntent.ActiveRentAction(DialogAction.DISMISS))

        mapVM.handleIntent(MapIntent.MapStart)
        advanceTimeBy(1000)
        mapVM.handleIntent(MapIntent.MapStop)
        advanceUntilIdle()

        val offset = 1
        val states = mapVM.mapUiStates.take(HANDLE_ACTIVE_RENT_STATES_NUM + offset).toList()

        assertEquals(false, (states[offset] as MapUiState.ActiveRent).show)
        assertEquals(false, (states[offset + 1] as MapUiState.FinishingRent).show)
        assertEquals(true, (states[offset + 2] as MapUiState.ActiveRentBar).show)
        assertEquals(false, (states[offset + 3] as MapUiState.QrScanButton).show)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun finishingRentShow() = runTest {
        val rent = Rent.empty().copy(rentStatus = MainRentStatus.CHECK_END)

        rentRepository.stub {
            onBlocking { checkActiveRent() } doReturn Result.Success(listOf(rent))
        }
        rentRepository.stub {
            on { getData() } doReturn RentData(Bike.empty())
        }

        mapVM.handleIntent(MapIntent.MapStart)
        advanceTimeBy(1000)
        mapVM.handleIntent(MapIntent.MapStop)
        advanceUntilIdle()

        val states = mapVM.mapUiStates.take(HANDLE_ACTIVE_RENT_STATES_NUM).toList()

        assertEquals(false, (states[0] as MapUiState.ActiveRent).show)
        assertEquals(true, (states[1] as MapUiState.FinishingRent).show)
        assertEquals(false, (states[2] as MapUiState.ActiveRentBar).show)
        assertEquals(false, (states[3] as MapUiState.QrScanButton).show)
    }

    companion object {
        private const val HANDLE_ACTIVE_RENT_STATES_NUM = 4
    }
}

// Reusable JUnit4 TestRule to override the Main dispatcher
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
