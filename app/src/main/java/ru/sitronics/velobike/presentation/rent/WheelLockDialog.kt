package ru.sitronics.velobike.presentation.rent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import coil.compose.rememberAsyncImagePainter
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.WheelLock
import ru.sitronics.velobike.presentation.map.toDialogState
import ru.sitronics.velobike.tools.getImageLoader

@Composable
fun BoxScope.WheelLockDialog(uiState: MapUiState, onClick: () -> Unit) {
    var state by remember { mutableStateOf(CLOSE) }

    if (uiState is WheelLock && state != CLOSING) {
        state = true.toDialogState()
    } else if (uiState !is WheelLock && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.White)
                .padding(all = 12.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(R.drawable.close_wheel_lock, getImageLoader()),
                contentDescription = null,
                modifier = Modifier.size(300.dp, 300.dp)
            )
            Button(
                onClick = { state = CLOSING; onClick() },
                modifier = Modifier
                    .width(250.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp)
            ) {
                Text(stringResource(R.string.ok_btn))
            }
        }
    }
}