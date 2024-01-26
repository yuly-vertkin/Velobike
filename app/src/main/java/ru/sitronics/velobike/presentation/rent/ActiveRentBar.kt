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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.ui.theme.HeaderBackgroundColor

@Composable
fun ActiveRentBar(uiState: MapUiState, onClick: () -> Unit) {
    val context = LocalContext.current
    var show by remember { mutableStateOf(false) }

    if (uiState is MapUiState.ActiveRentBar) {
        show = uiState.show
    }

    if (show) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp)
                .padding(horizontal = 16.dp)
                .clickable { onClick(); show = false }
                .clip(RoundedCornerShape(16.dp))
                .background(HeaderBackgroundColor)
                .padding(vertical = 16.dp)
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
                text = context.getString(R.string.return_to_active_ride),
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
