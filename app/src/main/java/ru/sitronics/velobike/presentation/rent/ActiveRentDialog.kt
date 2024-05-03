package ru.sitronics.velobike.presentation.rent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.rent.Rent
import ru.sitronics.velobike.presentation.SimpleBottomDialog
import ru.sitronics.velobike.presentation.details.BikeChargeSection
import ru.sitronics.velobike.presentation.map.DialogAction
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.ActiveRent
import ru.sitronics.velobike.presentation.map.toDialogState
import ru.sitronics.velobike.tools.getTimeStr

@Composable
fun ActiveRentDialog(uiState: MapUiState, onSizeChanged: (Int) -> Unit, onAction: (DialogAction) -> Unit) {
    var rent by remember { mutableStateOf<Rent?>(null) }
    var state by remember { mutableStateOf(CLOSE) }

    if (uiState is ActiveRent && state != CLOSING) {
        rent = uiState.rent
        state = uiState.show.toDialogState()
    } else if (uiState !is ActiveRent && state == CLOSING)
        state = CLOSE

    if (state == SHOW)
        rent?.let {
            SimpleBottomDialog(
                onSizeChanged = onSizeChanged,
                onDismissRequest = { state = CLOSING; onAction(DialogAction.DISSMISS) },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                ) {
                    Text(
                        text = "${it.cost} ₽",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f)
                    )

                    Text(
                        text = getTimeStr(it.startTime),
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f)
                    )

                    Text(
                        text = "№" + it.frameNumber,
                        modifier = Modifier
                            .padding(start = 16.dp)
                    )
                }

                it.bike?.let {
                    BikeChargeSection(it)
                }

                if (it.showFine) {
                    Text(
                        text = stringResource(R.string.rent_fine),
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                            .background(Color.Red, RoundedCornerShape(16.dp))
                            .padding(all = 16.dp)
                    )
                }

                if (!it.isOld) {
                    Button(
                        onClick = {
                            state = CLOSING; onSizeChanged(0)
                            onAction(DialogAction.CLICK)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 32.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Text(stringResource(R.string.active_rent_btn))
                    }
                }
            }
        }
}
