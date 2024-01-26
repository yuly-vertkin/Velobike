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

@Composable
fun ErrorDialog(uiState: MapUiState, onAction: () -> Unit) {
    var title by remember { mutableStateOf<String>("") }
    var text by remember { mutableStateOf<String>("") }
    var show by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (uiState is MapUiState.Error) {
        title = uiState.title
        text = uiState.text
        show = true
    }

    if (show) {
        AlertDialog(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text(text = title) },
            text = { Text(text = text) },
            onDismissRequest = { onAction(); show = false },
            confirmButton = {
                TextButton(
                    onClick = { onAction(); show = false }
                ) {
                    Text(context.getString(R.string.ok_btn))
                }
            },
        )
    }
}