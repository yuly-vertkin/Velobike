package ru.sitronics.velobike.presentation.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.SimpleBottomSheet
import ru.sitronics.velobike.presentation.SimpleDialog
import ru.sitronics.velobike.presentation.details.BikeDetailDialog
import ru.sitronics.velobike.presentation.details.StationDetailDialog
import ru.sitronics.velobike.presentation.rent.ActiveRentDialog
import ru.sitronics.velobike.presentation.rent.ScanQrCodeDialog
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

    Box(modifier = Modifier.padding(contentPadding)) {
        MapViewContainer(mapUiState, onAction)

        when (mapUiState) {
            is MapUiState.ShowBikeDetail -> {
                val uiState = mapUiState as MapUiState.ShowBikeDetail
                BikeDetailDialog(
                    bike = uiState.bike,
                    onDismiss = { onAction(MapIntent.CloseBikeDetail()) },
                    onClick = {
                        locationPermissionLauncher.runWithLocation(context) { lat, lon ->
                            onAction(MapIntent.CloseBikeDetail(uiState.bike.id, lat, lon))
                        }
                    }
                )
            }
            is MapUiState.ShowStationDetail -> {
                val uiState = mapUiState as MapUiState.ShowStationDetail
                StationDetailDialog(uiState.station) {
                    onAction(MapIntent.CloseParkingDetail())
                }
            }
            is MapUiState.ShowParkingDetail -> {
                val uiState = mapUiState as MapUiState.ShowParkingDetail
                SimpleBottomSheet(
                    onDismiss = { onAction(MapIntent.CloseParkingDetail()) },
                    onClick = { onAction(MapIntent.CloseParkingDetail(uiState.parking.id)) }
                )
            }
            is MapUiState.ShowQrScan -> {
                ScanQrCodeDialog(
                    onCancel = { onAction(MapIntent.CloseQrScan()) },
                    onAction = { bikeNumber ->
                        locationPermissionLauncher.runWithLocation(context) { lat, lon ->
                            onAction(MapIntent.CloseQrScan(bikeNumber, lat, lon))
                        }
                    },
                )
            }
            is MapUiState.ShowError -> {
                val uiState = mapUiState as MapUiState.ShowError
                SimpleDialog(
                    onDismissRequest = { onAction(MapIntent.CloseError) },
                    onConfirmation = { onAction(MapIntent.CloseError) },
                    dialogTitle = context.getString(R.string.start_omni_failed_default),
                    dialogText = uiState.error,
                    icon = Icons.Default.Info
                )
            }
            else -> {}
        }

        ActiveRentDialog(
            uiState = mapUiState,
            onDismiss = { onAction(MapIntent.CloseActiveRent(false)) },
            onClick = {
                locationPermissionLauncher.runWithLocation(context) { lat, lon ->
                    onAction(MapIntent.CloseActiveRent(true, lat, lon))
                }
            }
        )

        /*
                    if (uiState.dialogState) {
                        SimpleDialog(
                            onDismissRequest = { onAction(MainIntent.Dialog(true)) },
                            onConfirmation = { onAction(MainIntent.Dialog(true)) },
                            dialogTitle = "Alert dialog example",
                            dialogText = "This is an example of an alert dialog with buttons.",
                            icon = Icons.Default.Info
                        )
                    }
        */
    }
}
