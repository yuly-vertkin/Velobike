package ru.sitronics.velobike.presentation.map

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
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
import ru.sitronics.velobike.R
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.domain.auth.AuthManager
import ru.sitronics.velobike.domain.chat.ChatManager
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.MapContentRepository
import ru.sitronics.velobike.domain.map.MapContentUseCase
import ru.sitronics.velobike.domain.profile.ProfileUseCase
import ru.sitronics.velobike.domain.rent.Rent
import ru.sitronics.velobike.domain.rent.RentData
import ru.sitronics.velobike.domain.rent.RentRepository
import ru.sitronics.velobike.domain.rent.RentUseCase
import ru.sitronics.velobike.domain.rent.RentUseCaseImp

@RunWith(MockitoJUnitRunner::class)
class MapViewModelTest {
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun bikeTap() = runTest {
        val mapVM = MapViewModel(rentUC, mapUC, chatManager, context)
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
    fun handleActiveRent() = runTest {
        val rentRepository = mock<RentRepository>()
        val mapContentRepository = mock<MapContentRepository>()
        val profileUseCase = mock<ProfileUseCase>()
        val authManager = mock<AuthManager>()
        val rentUCImp = RentUseCaseImp(rentRepository, mapContentRepository, profileUseCase, authManager, context)
        val mapVM = MapViewModel(rentUCImp, mapUC, chatManager, context)

        rentRepository.stub {
            onBlocking { checkActiveRent() } doReturn Result.Success(listOf(Rent.empty()))
        }
        rentRepository.stub {
            on { getData() } doReturn RentData(Bike.empty())
        }

        mapVM.handleIntent(MapIntent.MapStart)
        advanceTimeBy(1000)
        mapVM.handleIntent(MapIntent.MapStop)
        advanceUntilIdle()

        assertTrue(mapVM.mapUiState.value is MapUiState.QrScanButton)
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
