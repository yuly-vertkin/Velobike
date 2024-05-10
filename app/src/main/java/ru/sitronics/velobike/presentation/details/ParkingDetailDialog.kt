package ru.sitronics.velobike.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yandex.mapkit.mapview.MapView
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.presentation.SimpleBottomDialog
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.CloseAllDetails
import ru.sitronics.velobike.presentation.map.MapUiState.ParkingDetail
import ru.sitronics.velobike.presentation.map.toDialogState
import ru.sitronics.velobike.tools.moveMapForObject
import ru.sitronics.velobike.ui.theme.HeaderBackgroundColor

@Composable
fun ParkingDetailDialog(
    mapView: MapView,
    uiState: MapUiState,
    onSizeChanged: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var parking by remember { mutableStateOf<Parking?>(null) }
    var state by remember { mutableStateOf(CLOSE) }

    val freePlaces = parking?.let { it.freeNonElectricSlots + it.freeElectricSlots + it.freeOmniSlots }

    if (uiState is ParkingDetail && state != CLOSING) {
        parking = uiState.parking
        state = true.toDialogState()
    } else if (uiState is CloseAllDetails)
        state = CLOSE
    else if (uiState !is ParkingDetail && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        SimpleBottomDialog(
            onSizeChanged = {
                moveMapForObject(mapView, parking!!.latitude, parking!!.longitude, it)
                onSizeChanged(it.height)
            },
            onDismissRequest = { state = CLOSING; onDismiss() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderBackgroundColor, RoundedCornerShape(16.dp, 16.dp))
                    .padding(all = 12.dp)
            ) {
                Text(
                    text = "№ ${parking?.id}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = parking?.address ?: "",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = freePlaces.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )

                Text(text = stringResource(R.string.free_places))
            }
        }
    }
}
