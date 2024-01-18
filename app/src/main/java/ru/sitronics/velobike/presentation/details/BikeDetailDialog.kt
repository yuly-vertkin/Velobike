package ru.sitronics.velobike.presentation.details

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.map.Bike
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

@SuppressLint("StringFormatInvalid")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeDetailDialog(bike: Bike, onDismiss: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Text(
            text = context.getString(R.string.bike_detail_title, bike.id),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 32.dp)
                .offset(y = (-12).dp)
        )

        Image(
            painter = painterResource(id = R.drawable.bike_detail),
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = context.getString(R.string.battery_power_percent, bike.batteryPower),
                modifier = Modifier
                    .padding(start = 32.dp)
                    .weight(1f)
            )

            Text(
                text = getPowerReserveText(context, bike),
                modifier = Modifier
                    .padding(start = 32.dp)
            )
        }

        Button(
            onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) onClick()
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 32.dp)
                .padding(vertical = 32.dp)
        ) {
            Text(context.getString(R.string.start_ride_btn))
        }
    }
}

private fun getPowerReserveText(context: Context, bike: Bike) : String {
    val remainingKm = (bike.batteryPower.toFloat() / 100) * MAX_BATTERY_KM
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

