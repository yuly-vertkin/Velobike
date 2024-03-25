package ru.sitronics.velobike.presentation.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.sitronics.velobike.presentation.details.BikeDetailDialog
import ru.sitronics.velobike.presentation.details.ParkingDetailDialog
import ru.sitronics.velobike.presentation.details.StationDetailDialog
import ru.sitronics.velobike.presentation.rent.ActiveRentBar
import ru.sitronics.velobike.presentation.rent.ActiveRentDialog
import ru.sitronics.velobike.presentation.rent.ChooseParkingDialog
import ru.sitronics.velobike.presentation.rent.FinishedRentDialog
import ru.sitronics.velobike.presentation.rent.FinishingRentDialog
import ru.sitronics.velobike.presentation.rent.LoadingBar
import ru.sitronics.velobike.presentation.rent.ScanQrCodeDialog
import ru.sitronics.velobike.presentation.rent.TakePhotoScreen
import ru.sitronics.velobike.presentation.rent.WheelLockDialog
import ru.sitronics.velobike.tools.Logg
import ru.sitronics.velobike.tools.rememberLocationPermissionLauncher
import ru.sitronics.velobike.tools.runWithLocation

@Composable
fun MapScreen(
    contentPadding: PaddingValues,
    mapViewModel: MapViewModel = viewModel()
) {
    val mapUiState by mapViewModel.mapUiState.collectAsStateWithLifecycle()
    val onAction: (MapIntent) -> Unit = { intent -> mapViewModel.handleIntent(intent) }
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLocationPermissionLauncher()
    var padding by remember { mutableStateOf(Padding(0, 0)) }

    Box(modifier = Modifier.padding(contentPadding)) {
        Logg.d("!!!! MapScreen ${mapUiState.javaClass.name}")

        MapViewContainer(mapUiState, padding, onAction)

        BikeDetailDialog(mapUiState, { padding = padding.copy(bottom = it) }) { action, id, from ->
            locationPermissionLauncher.runWithLocation(context) { lat, lon ->
                onAction(MapIntent.BikeDetailAction(action, id, from, lat, lon))
            }
        }

        StationDetailDialog(mapUiState, { padding = padding.copy(bottom = it) }) { onAction(MapIntent.ResetState) }

        ParkingDetailDialog(mapUiState, { padding = padding.copy(bottom = it) }) { onAction(MapIntent.ResetState) }

        SearchDialog(mapUiState, { onAction(MapIntent.SearchAction(it)) }) {
            locationPermissionLauncher.runWithLocation(context) { lat, lon ->
                onAction(MapIntent.Search(it, lat, lon))
            }
        }

        ScanQrCodeDialog(mapUiState) { action, id, from ->
            locationPermissionLauncher.runWithLocation(context) { lat, lon ->
                onAction(MapIntent.ScanQrAction(action, id, from, lat, lon))
            }
        }

        ActiveRentBar(mapUiState, { padding = padding.copy(top = it) }) { onAction(MapIntent.ClickActiveRentBar) }

        ActiveRentDialog(mapUiState, { padding = padding.copy(bottom = it) }) {
            locationPermissionLauncher.runWithLocation(context) { lat, lon ->
                onAction(MapIntent.ActiveRentAction(it, lat, lon))
            }
        }

        FinishingRentDialog(mapUiState, { padding = padding.copy(bottom = it) }) {
            onAction(MapIntent.FinishingRentAction(it))
        }

        ChooseParkingDialog(mapUiState, { padding = padding.copy(bottom = it) }) {
            onAction(MapIntent.ChooseParkingAction(it))
        }

        WheelLockDialog(mapUiState) { onAction(MapIntent.WheelLockAction) }

        TakePhotoScreen(mapUiState) { action, path ->
            onAction(MapIntent.OnTakePhoto(action, path))
        }

        FinishedRentDialog(mapUiState, { padding = padding.copy(bottom = it) }) { action, rent, rating ->
            onAction(MapIntent.FinishedRentAction(action, rent, rating))
        }

        ErrorDialog(mapUiState) { onAction(MapIntent.ResetState) }

        LoadingBar(mapUiState)
    }
}
