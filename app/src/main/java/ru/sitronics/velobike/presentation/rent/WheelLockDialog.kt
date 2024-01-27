package ru.sitronics.velobike.presentation.rent

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.WheelLock
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.toDialogState

@Composable
fun BoxScope.WheelLockDialog(uiState: MapUiState, onClick: () -> Unit) {
    var state by remember { mutableStateOf(CLOSE) }
    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28)
                    add(ImageDecoderDecoder.Factory())
                else
                    add(GifDecoder.Factory())
            }
            .build()
    }

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
                painter = rememberAsyncImagePainter(R.drawable.close_wheel_lock, imageLoader),
                contentDescription = null,
                modifier = Modifier
                    .width(300.dp)
                    .height(300.dp)
            )
            Button(
                onClick = { state = CLOSING; onClick() },
                modifier = Modifier
                    .width(250.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp)
            ) {
                Text(context.getString(R.string.ok_btn))
            }
        }
    }
}