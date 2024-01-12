package ru.sitronics.velobike.presentation.map

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yandex.mapkit.mapview.MapView
import ru.sitronics.velobike.CHANGE_ZOOM
import ru.sitronics.velobike.R
import ru.sitronics.velobike.tools.rememberLocationPermissionLauncher
import ru.sitronics.velobike.tools.runWithLocation

@Composable
fun BoxScope.MapTopLayerContainer(
    mapView: MapView,
    uiState: MapUiState,
    onAction: (MapIntent) -> Unit,
) {
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLocationPermissionLauncher()

    FloatingActionButton(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = 8.dp, y = 16.dp),
        onClick = {  },
        shape = CircleShape,
        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        Icon(
            painter = painterResource(R.drawable.chat),
            contentDescription = "",
            tint = Color.Unspecified,
        )
    }

    FloatingActionButton(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = (-8).dp, y = 16.dp),
        onClick = {  },
        shape = CircleShape,
        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        Icon(painterResource(R.drawable.search), "")
    }

    FloatingActionButton(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .offset(x = 8.dp, y = (-16).dp),
        onClick = {  },
        shape = CircleShape,
        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        Icon(painterResource(R.drawable.layers), "")
    }

    FloatingActionButton(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .offset(x = (-8).dp, y = (-32).dp),
        onClick = { changeZoom(mapView, CHANGE_ZOOM) },
        shape = CircleShape,
        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        Icon(painterResource(R.drawable.plus), "")
    }

    FloatingActionButton(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .offset(x = (-8).dp, y = 32.dp),
        onClick = { changeZoom(mapView, -CHANGE_ZOOM) },
        shape = CircleShape,
        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        Icon(painterResource(R.drawable.minus), "")
    }

    FloatingActionButton(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = (-16).dp),
        onClick = { onAction(MapIntent.QrScanTap) },
        shape = CircleShape,
        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        Icon(
            painter = painterResource(R.drawable.qr),
            contentDescription = "",
            tint = Color.Unspecified,
        )
    }

    FloatingActionButton(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = (-8).dp, y = (-16).dp),
        onClick = {
            locationPermissionLauncher.runWithLocation(context) { lat, lon ->
                moveMap(mapView, lat ?: MOSCOW_LAT, lon ?: MOSCOW_LON)
            }
        },
        shape = CircleShape,
        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        Icon(painterResource(R.drawable.to_user_position), "")
    }
}

/*
@Preview(showBackground = true, widthDp = 500, heightDp = 500)
@Composable
fun MapTopLayerContainerPreview() {
    VelobikeTheme {
        Box {
            MapTopLayerContainer(MainUiState.Normal) {}
        }
    }
}
*/
