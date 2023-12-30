package ru.sitronics.velobike.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat

@Composable
fun rememberCameraPermissionLauncher() : ActivityResultLauncher<String> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                runAction?.invoke()
            }
        })
}

fun ActivityResultLauncher<String>.runWithCamera(context: Context, action: () -> Unit) {
    if (!isCameraPermissionGranted(context)) {
        runAction = action
        launch(cameraPermission)
    } else {
        action()
    }
}

fun isCameraPermissionGranted(context: Context): Boolean {
    return  ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED
}

private const val cameraPermission = Manifest.permission.CAMERA

private var runAction: (() -> Unit)? = null
