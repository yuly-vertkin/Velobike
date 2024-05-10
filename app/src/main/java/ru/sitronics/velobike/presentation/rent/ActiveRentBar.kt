package ru.sitronics.velobike.presentation.rent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.ActiveRentBar
import ru.sitronics.velobike.presentation.map.toDialogState
import ru.sitronics.velobike.tools.dpToPx
import ru.sitronics.velobike.ui.theme.HeaderBackgroundColor

@Composable
fun ActiveRentBar(uiState: MapUiState, onSizeChanged: (Int) -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(CLOSE) }

    if (uiState is ActiveRentBar && state != CLOSING) {
        state = uiState.show.toDialogState()
        if (!uiState.show) onSizeChanged(0)
    } else if (uiState !is ActiveRentBar && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .padding(horizontal = 16.dp)
                .clickable { state = CLOSING; onSizeChanged(0); onClick() }
                .clip(RoundedCornerShape(16.dp))
                .background(HeaderBackgroundColor)
                .padding(vertical = 16.dp)
                .onGloballyPositioned { coordinates ->
                    // size correction: top padding = 32 + vertical paddings 16 + 16 = 32
                    val height = coordinates.size.height + 64.dpToPx(context)
                    onSizeChanged(height)
                }
        ) {
            Icon(
                painter = painterResource(R.drawable.return_to_active_ride),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(40.dp)
            )

            Text(
                text = stringResource(R.string.return_to_active_ride),
                fontSize = 13.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            )

            Icon(
                painter = painterResource(R.drawable.arrow_right),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp)
            )
        }
    }
}
