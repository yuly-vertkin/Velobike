package ru.sitronics.velobike.presentation.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.presentation.SimpleBottomDialog
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.StationDetail
import ru.sitronics.velobike.presentation.map.toDialogState
import ru.sitronics.velobike.ui.theme.HeaderBackgroundColor

@Composable
fun StationDetailDialog(uiState: MapUiState, onSizeChanged: (Int) -> Unit, onDismiss: () -> Unit) {
    var parking by remember { mutableStateOf<Parking?>(null) }
    var state by remember { mutableStateOf(CLOSE) }

    val availableBikes = parking?.let { it.availableNonElectricBikes + it.availableElectricBikes + it.availableOmniBikes }
    val freePlaces = parking?.let { it.freeNonElectricSlots + it.freeElectricSlots + it.freeOmniSlots }

    if (uiState is StationDetail && state != CLOSING) {
        parking = uiState.station
        state = true.toDialogState()
    } else if (uiState !is StationDetail && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        SimpleBottomDialog(
            onSizeChanged = onSizeChanged,
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
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = availableBikes.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(text = stringResource(R.string.bikes_available))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = freePlaces.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(text = stringResource(R.string.free_places))
                }
            }
        }
    }
}
