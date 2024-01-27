package ru.sitronics.velobike.presentation.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.map.MapUiState.Error
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE

@Composable
fun ErrorDialog(uiState: MapUiState, onAction: () -> Unit) {
    var title by remember { mutableStateOf<String>("") }
    var text by remember { mutableStateOf<String>("") }
    var state by remember { mutableStateOf(CLOSE) }
    val context = LocalContext.current

    if (uiState is Error && state != CLOSING) {
        title = uiState.title
        text = uiState.text
        state = true.toDialogState()
    } else if (uiState !is Error && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        AlertDialog(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text(text = title) },
            text = { Text(text = text) },
            onDismissRequest = { state = CLOSING; onAction() },
            confirmButton = {
                TextButton(
                    onClick = { state = CLOSING; onAction() }
                ) {
                    Text(context.getString(R.string.ok_btn))
                }
            },
        )
    }
}