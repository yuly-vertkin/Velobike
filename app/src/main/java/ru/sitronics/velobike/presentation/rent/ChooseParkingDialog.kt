package ru.sitronics.velobike.presentation.rent

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import ru.sitronics.velobike.presentation.map.MapUiState.ChooseParking
import ru.sitronics.velobike.presentation.map.toDialogState

@Composable
fun ChooseParkingDialog(uiState: MapUiState, onSizeChanged: (Int) -> Unit, onAction: (DialogAction) -> Unit) {
    var state by remember { mutableStateOf(CLOSE) }

    if (uiState is ChooseParking && state != CLOSING) {
        state = true.toDialogState()
    } else if (uiState !is ChooseParking && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        SimpleBottomDialog(
            onSizeChanged = { onSizeChanged(it.height) },
            onDismissRequest = { state = CLOSING; onAction(DialogAction.DISSMISS) },
        ) {
            Text(
                text = stringResource(R.string.choose_parking_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(all = 16.dp)
            )

            Text(
                text = stringResource(R.string.choose_parking_text),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            )

            OutlinedButton(
                onClick = {
                    state = CLOSING; onSizeChanged(0)
                    onAction(DialogAction.CLICK)
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(all = 16.dp)
            ) {
                Text(stringResource(R.string.choose_parking_btn))
            }
        }
    }
}
