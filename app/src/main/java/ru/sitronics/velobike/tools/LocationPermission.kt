package ru.sitronics.velobike.tools

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.LocationServices

@Composable
fun rememberLocationPermissionLauncher() : ActivityResultLauncher<Array<String>> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (granted) {
                runWithLocationInt(context, runAction)
            } else {
                runAction?.invoke(null, null)
            }
        })
}

@Composable
fun ActivityResultLauncher<Array<String>>.RunWithLocation(action: (Double?, Double?) -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(key1 = lifecycle, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (!areLocationPermissionGranted(context)) {
                    runAction = action
                    launch(locationPermissions)
                } else {
                    runWithLocationInt(context, action)
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    })
}

fun ActivityResultLauncher<Array<String>>.runWithLocation(context: Context, action: (Double?, Double?) -> Unit) {
    if (!areLocationPermissionGranted(context)) {
        runAction = action
        launch(locationPermissions)
    } else {
        runWithLocationInt(context, action)
    }
}

@SuppressLint("MissingPermission")
private fun runWithLocationInt(context: Context, action: ((Double?, Double?) -> Unit)?) {
    LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener { location: Location? ->
        action?.invoke(location?.latitude, location?.longitude)
    }
}

private fun areLocationPermissionGranted(context: Context): Boolean {
    return  ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)
private var runAction: ((Double?, Double?) -> Unit)? = null
