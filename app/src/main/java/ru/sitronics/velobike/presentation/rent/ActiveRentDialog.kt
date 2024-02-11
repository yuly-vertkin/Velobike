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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.rent.ActiveRent
import ru.sitronics.velobike.presentation.details.BikeChargeSection
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.CurrentRent
import ru.sitronics.velobike.presentation.map.toDialogState
import ru.sitronics.velobike.tools.getDateTimeStr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRentDialog(uiState: MapUiState, onDismiss: () -> Unit, onClick: () -> Unit) {
    var activeRent by remember { mutableStateOf<ActiveRent?>(null) }
    var state by remember { mutableStateOf(CLOSE) }
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (uiState is CurrentRent && state != CLOSING) {
        activeRent = uiState.activeRent
        state = uiState.show.toDialogState()
    } else if (uiState !is CurrentRent && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        ModalBottomSheet(
            onDismissRequest = { state = CLOSING; onDismiss() },
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
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            state = CLOSING
                            onClick()
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 32.dp)
                    .padding(vertical = 32.dp)
            ) {
                Text(context.getString(R.string.active_rent_btn))
            }
        }
    }
}

private fun getTimeStr(startTime: Long) : String {
    val time = System.currentTimeMillis() - startTime
    return getDateTimeStr(time, "HH:mm")
}
