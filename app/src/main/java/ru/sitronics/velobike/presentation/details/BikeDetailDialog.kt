package ru.sitronics.velobike.presentation.details

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yandex.mapkit.mapview.MapView
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.presentation.SimpleBottomDialog
import ru.sitronics.velobike.presentation.map.DialogAction
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.BikeDetail
import ru.sitronics.velobike.presentation.map.MapUiState.CloseAllDetails
import ru.sitronics.velobike.presentation.map.toDialogState
import ru.sitronics.velobike.tools.moveMapForObject
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

@SuppressLint("StringFormatInvalid")
@Composable
fun BikeDetailDialog(
    mapView: MapView,
    uiState: MapUiState,
    onSizeChanged: (Int) -> Unit,
    onAction: (DialogAction, String?, Boolean) -> Unit
) {
    var bike by remember { mutableStateOf<Bike?>(null) }
    var fromQrScan by remember { mutableStateOf(false) }
    var enableAction by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf(CLOSE) }

    if (uiState is BikeDetail && state != CLOSING) {
        bike = uiState.bike
        fromQrScan = uiState.fromQrScan
        enableAction = uiState.enableAction
        state = true.toDialogState()
    } else if (uiState is CloseAllDetails)
        state = CLOSE
    else if (uiState !is BikeDetail && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        SimpleBottomDialog(
            onSizeChanged = {
                moveMapForObject(mapView, bike!!.latitude, bike!!.longitude, it)
                onSizeChanged(it.height)
            },
            onDismissRequest = { state = CLOSING; onAction(DialogAction.DISSMISS, bike?.id, fromQrScan) },
        ) {
            Text(
                text = stringResource(R.string.bike_detail_title, bike?.id.orEmpty()),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(all = 16.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.bike_detail),
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            BikeChargeSection(bike)

            Button(
                onClick = {
                    state = CLOSING; onSizeChanged(0)
                    onAction(DialogAction.CLICK, bike?.id, fromQrScan)
                },
                enabled = enableAction,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 32.dp)
                    .padding(vertical = 32.dp)
            ) {
                Text(stringResource(R.string.start_ride_btn))
            }
        }
    }
}

@SuppressLint("StringFormatInvalid")
@Composable
fun BikeChargeSection(bike: Bike?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.battery_power_percent, bike?.batteryPower ?: 0),
            modifier = Modifier
                .padding(start = 32.dp)
                .weight(1f)
        )

        Text(
            text = getPowerReserveText(LocalContext.current, bike),
            modifier = Modifier
                .padding(start = 32.dp)
        )
    }
}

private fun getPowerReserveText(context: Context, bike: Bike?) : String {
    val remainingKm = bike?.let { (it.batteryPower.toFloat() / 100) * MAX_BATTERY_KM } ?: 0f
    val remainingMinutes = (remainingKm * 60 / BIKE_AVG_SPEED_KM_H).roundToInt().minutes
    val hours = remainingMinutes.inWholeHours
    val minutes = remainingMinutes.inWholeMinutes % 60

    val timeText = context.getString(R.string.remaining_mileage_time_h_m,
        if(hours < 10) String.format("%02d", hours) else hours,
        if(minutes < 10) String.format("%02d", minutes) else minutes)
    return context.getString(R.string.remaining_mileage_km_time, remainingKm, timeText)
}

private const val MAX_BATTERY_KM = 52.0f
const val BIKE_AVG_SPEED_KM_H = 25.0f

