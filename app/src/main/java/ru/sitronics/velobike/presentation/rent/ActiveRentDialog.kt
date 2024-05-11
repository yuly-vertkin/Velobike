package ru.sitronics.velobike.presentation.rent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var showPortPlus by remember { mutableStateOf(false) }

    if (uiState is ActiveRent && state != CLOSING) {
        rent = uiState.rent
        state = uiState.show.toDialogState()
    } else if (uiState !is ActiveRent && state == CLOSING)
        state = CLOSE

    if (state == SHOW)
        rent?.let {
            if (!showPortPlus) {
                SimpleBottomDialog(
                    onSizeChanged = { onSizeChanged(it.height) },
                    onDismissRequest = { state = CLOSING; onAction(DialogAction.DISMISS) },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp)
                    ) {
                        Text(
                            text = "${it.cost} ₽",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f)
                        )

                        Text(
                            text = getTimeStr(it.startTime),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f)
                        )

                        Text(
                            text = "№" + it.frameNumber,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
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

                    HorizontalDivider(Modifier.padding(vertical = 1.dp), 1.dp, Color.LightGray)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp)
                    ) {
                        if (it.isOld) {
                            Button(
                                onClick = { showPortPlus = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.port_plus),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.park_btn),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    state = CLOSING; onSizeChanged(0)
                                    onAction(DialogAction.CLICK)
                                },
                                modifier = Modifier.padding(start = 8.dp).weight(1f)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.port_plus),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.near_free_btn))
                            }
                        } else {
                            Button(
                                onClick = {
                                    state = CLOSING; onSizeChanged(0)
                                    onAction(DialogAction.CLICK)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.stop_sign),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.active_rent_btn),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }

                            Button(
                                onClick = { onAction(DialogAction.BACK) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(10.dp),
                                modifier = Modifier.padding(start = 8.dp).weight(1f)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.wheel),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.unlock_wheel_btn),
                                    color = Color.Blue,
                                    fontSize = 12.sp,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            } else {
                PortPlusDialog { showPortPlus = false }
            }
        }
}
