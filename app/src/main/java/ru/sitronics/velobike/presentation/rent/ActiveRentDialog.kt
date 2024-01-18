package ru.sitronics.velobike.presentation.rent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.ui.theme.HeaderBackgroundColor

@Composable
fun ActiveRentDialog(uiState: MapUiState, onShow: () -> Unit, onDismiss: () -> Unit, onClick: () -> Unit) {
    var showActiveRent by remember { mutableStateOf(false) }
    var isDialogClosed by remember { mutableStateOf(false) }

    if (uiState is MapUiState.ShowActiveRent) {
        showActiveRent = uiState.activeRent != null
    }

    if (showActiveRent) {
        if (!isDialogClosed) {
            ActiveRentDialog({
                isDialogClosed = true; onDismiss()
            }, {
                isDialogClosed = true; onClick()
            })
        } else {
            ActiveRentBar {
                isDialogClosed = false; onShow()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveRentDialog(onDismiss: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Text(
            text = "Rent",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 32.dp)
                .offset(y = (-12).dp)
        )

        Button(
            onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) onClick()
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 32.dp)
                .padding(vertical = 32.dp)
        ) {
            Text("Close")
        }
    }
}

@Composable
private fun ActiveRentBar(onClick: () -> Unit) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp)
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
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
