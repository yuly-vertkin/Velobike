package ru.sitronics.velobike.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.ParkingDetail
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.toDialogState
import ru.sitronics.velobike.ui.theme.HeaderBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingDetailDialog(uiState: MapUiState, onDismiss: () -> Unit) {
    var parking by remember { mutableStateOf<Parking?>(null) }
    var state by remember { mutableStateOf(CLOSE) }
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    val freePlaces = parking?.let { it.freeNonElectricSlots + it.freeElectricSlots + it.freeOmniSlots }

    if (uiState is ParkingDetail && state != CLOSING) {
        parking = uiState.parking
        state = true.toDialogState()
    } else if (uiState !is ParkingDetail && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        ModalBottomSheet(
            onDismissRequest = { state = CLOSING; onDismiss() },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-48).dp)
                    .background(HeaderBackgroundColor)
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

                Text(text = context.getString(R.string.free_places))
            }
        }
    }
}
