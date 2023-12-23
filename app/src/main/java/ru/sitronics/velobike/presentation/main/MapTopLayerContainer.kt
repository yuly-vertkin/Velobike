package ru.sitronics.velobike.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.sitronics.velobike.tools.rememberLocationPermissionLauncher
import ru.sitronics.velobike.tools.runWithLocation
import ru.sitronics.velobike.ui.theme.VelobikeTheme

@Composable
fun BoxScope.MapTopLayerContainer(
    uiState: MainUiState,
    onAction: (MainIntent) -> Unit,
) {
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLocationPermissionLauncher()

    FloatingActionButton(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .offset(y = -100.dp),
        onClick = { /*onAction(MainIntent.Action1(true))*/ },
        shape = CircleShape,
        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        Icon(Icons.Filled.Warning, "Localized description")
    }

    FloatingActionButton(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .offset(y = 32.dp),
        onClick = { /*onAction(MainIntent.Action2(true))*/ },
        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        Icon(Icons.Filled.Add, "Localized description")
    }

    FloatingActionButton(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(y = (-32).dp),
        onClick = {
            locationPermissionLauncher.runWithLocation(context) { lat, lon -> println("!!! Location: $lat, $lon") }
//            onAction(MainIntent.Action3)
        },
        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        Icon(Icons.Filled.Info, "Localized description")
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 500)
@Composable
fun MapTopLayerContainerPreview() {
    VelobikeTheme {
        Box {
            MapTopLayerContainer(MainUiState.Normal) {}
        }
    }
}
