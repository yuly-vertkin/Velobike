package ru.sitronics.velobike.presentation.rent

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.map.DialogAction
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.FinishingRent
import ru.sitronics.velobike.presentation.map.toDialogState
import ru.sitronics.velobike.tools.onSizeChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishingRentDialog(uiState: MapUiState, onSizeChanged: (Int) -> Unit, onAction: (DialogAction) -> Unit) {
    var state by remember { mutableStateOf(CLOSE) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    sheetState.onSizeChanged(onSizeChanged)

    if (uiState is FinishingRent && state != CLOSING) {
        state = uiState.show.toDialogState()
    } else if (uiState !is FinishingRent && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        ModalBottomSheet(
            onDismissRequest = { state = CLOSING; onAction(DialogAction.DISSMISS) },
            sheetState = sheetState
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-16).dp)
            ) {
                IconButton(onClick = { state = CLOSING; onAction(DialogAction.BACK) }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = null,
                        tint = Color.LightGray,
                    )
                }

                Text(
                    text = context.getString(R.string.finish_rent),
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
                    text = context.getString(R.string.close_wheel_lock),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                )

                Text(
                    text = context.getString(R.string.close_chain),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                )
            }

            Button(
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            state = CLOSING
                            onAction(DialogAction.CLICK)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 32.dp)
                    .padding(vertical = 32.dp)
            ) {
                Text(context.getString(R.string.finish_rent_btn))
            }
        }
    }
}
