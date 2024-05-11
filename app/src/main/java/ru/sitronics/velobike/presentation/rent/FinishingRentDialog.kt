package ru.sitronics.velobike.presentation.rent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.SimpleBottomDialog
import ru.sitronics.velobike.presentation.map.DialogAction
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.FinishingRent
import ru.sitronics.velobike.presentation.map.toDialogState

@Composable
fun FinishingRentDialog(uiState: MapUiState, onSizeChanged: (Int) -> Unit, onAction: (DialogAction) -> Unit) {
    var state by remember { mutableStateOf(CLOSE) }

    if (uiState is FinishingRent && state != CLOSING) {
        state = uiState.show.toDialogState()
    } else if (uiState !is FinishingRent && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
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
                IconButton(onClick = { state = CLOSING; onAction(DialogAction.BACK) }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = null,
                        tint = Color.LightGray,
                    )
                }

                Text(
                    text = stringResource(R.string.finish_rent),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = (-24).dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.close_wheel_lock),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                )

                Text(
                    text = stringResource(R.string.close_chain),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                )
            }

            Button(
                onClick = {
                    state = CLOSING; onSizeChanged(0)
                    onAction(DialogAction.CLICK)
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(all = 16.dp)
            ) {
                Text(stringResource(R.string.finish_rent_btn))
            }
        }
    }
}
