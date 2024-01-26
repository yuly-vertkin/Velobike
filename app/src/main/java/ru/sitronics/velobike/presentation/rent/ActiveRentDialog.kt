package ru.sitronics.velobike.presentation.rent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.rent.ActiveRent
import ru.sitronics.velobike.presentation.details.BikeChargeSection
import ru.sitronics.velobike.presentation.map.MapUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRentDialog(uiState: MapUiState, onDismiss: () -> Unit, onClick: () -> Unit) {
    var activeRent by remember { mutableStateOf<ActiveRent?>(null) }
    var show by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    if (uiState is MapUiState.Show) {
        activeRent = uiState.activeRent
        show = uiState.show
    }

    if (show) {
        ModalBottomSheet(
            onDismissRequest = { show = false; onDismiss() },
            sheetState = sheetState
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "100 p.",
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                )

                Text(
                    text = getTimeStr(activeRent?.startTime ?: 0),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                )

                Text(
                    text = "№" + activeRent?.frameNumber,
                    modifier = Modifier
                        .padding(start = 16.dp)
                )
            }

            activeRent?.bike?.let {
                BikeChargeSection(it)
            }

            Button(
                onClick = { onClick() },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 32.dp)
                    .padding(vertical = 32.dp)
            ) {
                Text(context.getString(R.string.finish_rent))
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

private fun getTimeStr(startTime: Long) : String {
    val time = System.currentTimeMillis() - startTime
    return timeFormat.format(Date(time))
}
