package ru.sitronics.velobike.presentation.main

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.AuthManager
import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.domain.content.MapContentRepository
import ru.sitronics.velobike.presentation.BaseViewModel
import ru.sitronics.velobike.tools.Logg
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mapContentRepository: MapContentRepository,
    private val authManager: AuthManager,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    private val _mainUiState: MutableStateFlow<MainUiState> = MutableStateFlow(
        if (authManager.isLogged) MainUiState.Normal else MainUiState.Login
    )
    val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.Logged -> {
                _mainUiState.value = MainUiState.Normal
            }
            is MainIntent.ChangeMapPosition -> {
                if (intent.zoom >= SHOW_CONTENT_ZOOM)
                    updateBikesAndParkings(intent.mapRect)
            }
            is MainIntent.TapMapObject -> {
                when (intent.userData) {
                    is MarkerUserData.Bike -> onBikeClick(intent.userData.id)
                    is MarkerUserData.Parking -> onParkingClick(intent.userData.id)
                    is MarkerUserData.SlowZone -> {}//onSlowZoneClick(data)
                    is MarkerUserData.MoveZone -> {}//onNotMoveZoneClick()
                    null -> {}
                }
            }
            is MainIntent.CloseBikeDetail -> {
                _mainUiState.value = MainUiState.Normal
            }
        }
    }

    private fun onBikeClick(id: String) {
        println("!!! onBikeClick $id")
        val bike = mapContentRepository.getData().bikes?.find { it.id == id } ?: return
        _mainUiState.value = MainUiState.ShowBikeDetail(bike)
    }

    private fun onParkingClick(id: String) {
        println("!!! onParkingClick $id")
    }

    private fun updateBikesAndParkings(mapRect: MapRect) {
//        Logg.d("!!! mapRect: ${mapRect.startLat}, ${mapRect.startLong}, ${mapRect.endLat}, ${mapRect.endLong}")
        processNetworkCall(
            action = { mapContentRepository.getBikes(mapRect) },
            onSuccess = {
                Logg.d("!!! getBikes() ${it.size}")
                mapContentRepository.saveData(mapContentRepository.getData().copy(
                    bikes = it
                ))
                _mainUiState.value = MainUiState.BikesUpdated(it)
            },
            onError = { Logg.d("!!! ERROR getBikes()") },
            force = true,
        )

        processNetworkCall(
            action = { mapContentRepository.getParkings(mapRect) },
            onSuccess = {
                Logg.d("!!! getParkings() ${it.size}")
                mapContentRepository.saveData(mapContentRepository.getData().copy(
                    parkings = it
                ))
                _mainUiState.value = MainUiState.ParkingsUpdated(it)
            },
            onError = { Logg.d("!!! ERROR getParkings() ${it.message}") },
            force = true,
            callName = DEFAULT_CALL_NAME + "2",
        )
    }

    companion object {
        private const val SHOW_CONTENT_ZOOM = 5f
    }
}

// old:
/*
    private val _mainUiState = MutableStateFlow(MainUiState())

    init {
        _mainUiState.value = MainUiState(isLogged = authManager.isLogged)
    }

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.Login -> {
                // ... some work here
                _mainUiState.update { currentState ->
                    currentState.copy(isLogged = authManager.isLogged)
                }
            }
            is MainIntent.ChangeMapPosition -> {
                if (intent.zoom >= SHOW_CONTENT_ZOOM)
                    updateBikesAndParkings(intent.mapRect)
            }
            is MainIntent.BikesRendered -> {
                _mainUiState.update { currentState ->
                    currentState.copy(bikesUpdated = false)
                }
            }
            is MainIntent.ParkingsRendered -> {
                _mainUiState.update { currentState ->
                    currentState.copy(parkingsUpdated = false)
                }
            }
            // temp
            is MainIntent.Action1 -> {
                // ... some work here
                _mainUiState.update { currentState ->
                    currentState.copy(dialogState = true)
                }
            }
            is MainIntent.Action2 -> {
                // ... some work here
                _mainUiState.update { currentState ->
                    currentState.copy(bottomSheetState = true)
                }
            }
            is MainIntent.Action3 -> {
                _mainUiState.update { currentState ->
                    currentState.copy(bottomSheetState = true)
                }
            }
            is MainIntent.Dialog -> {
                // ... some work here
                _mainUiState.update { currentState ->
                    currentState.copy(dialogState = false)
                }
            }
            is MainIntent.BottomSheet -> {
                // ... some work here
                _mainUiState.update { currentState ->
                    currentState.copy(bottomSheetState = false)
                }
            }
        }
    }
                _mainUiState.update { currentState ->
                    currentState.copy(bikes = it, bikesUpdated = true)
                }

*/
