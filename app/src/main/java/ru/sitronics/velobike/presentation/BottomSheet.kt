package ru.sitronics.velobike.presentation

import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleBottomSheet(onDismissRequest: () -> Unit, onClick: () -> Unit,) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        // Sheet content
//        var text by remember { mutableStateOf("") }
//
//        OutlinedTextField(
//            value = text,
//            onValueChange = { text = it },
//            label = { Text("Test") },
//            modifier = Modifier.width(300.dp)
//        )

        Button(onClick = {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onClick()
                }
            }
        }) {
            Text("Hide bottom sheet")
        }
    }
}
