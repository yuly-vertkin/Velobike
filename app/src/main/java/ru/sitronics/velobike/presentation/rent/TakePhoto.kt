package ru.sitronics.velobike.presentation.rent

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import ru.sitronics.velobike.BuildConfig
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.TakePhoto
import ru.sitronics.velobike.presentation.map.toDialogState
import ru.sitronics.velobike.tools.rememberCameraPermissionLauncher
import ru.sitronics.velobike.tools.runWithCamera
import java.io.File

@Composable
fun TakePhoto(uiState: MapUiState, onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
    var state by remember { mutableStateOf(CLOSE) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var filePath by remember { mutableStateOf("") }
    val cameraPermissionLauncher = rememberCameraPermissionLauncher()
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        state = CLOSING
        if (success) {
            onSuccess(filePath)
        } else {
            onDismiss()
        }
    }

    if (uiState is TakePhoto && state != CLOSING) {
        state = true.toDialogState()
    } else if (uiState !is TakePhoto && state == CLOSING) {
        state = CLOSE
    }

    if (state == SHOW) {
        val file = remember { File.createTempFile("vel_", ".jpg", context.externalCacheDir) }
        val uri = remember { FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file) }
        filePath = file.absolutePath

        scope.launch {
            cameraPermissionLauncher.runWithCamera(context) {
                cameraLauncher.launch(uri)
            }
        }
    }
}
