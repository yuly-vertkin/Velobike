package ru.sitronics.velobike.presentation.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView
import ru.sitronics.velobike.CHANGE_ZOOM
import ru.sitronics.velobike.R
import ru.sitronics.velobike.tools.drawChatBitmap
import ru.sitronics.velobike.tools.getBitmapFromVectorDrawable
import ru.sitronics.velobike.tools.rememberLocationPermissionLauncher
import ru.sitronics.velobike.tools.runWithLocation

@Composable
fun BoxScope.MapTopLayerContainer(
    mapView: MapView,
    uiState: MapUiState,
    padding: Padding,
    onAction: (MapIntent) -> Unit,
) {
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLocationPermissionLauncher()
    var showQrScanButton by remember { mutableStateOf(true) }
    val chatDefaultBitmap = remember { context.getBitmapFromVectorDrawable(R.drawable.chat).asImageBitmap() }
    val chatUnreadMessagesBitmap = remember { context.getBitmapFromVectorDrawable(R.drawable.chat).drawChatBitmap().asImageBitmap() }
    val chatBitmap = remember { mutableStateOf(chatDefaultBitmap) }

    if (uiState is MapUiState.QrScanButton) {
        showQrScanButton = uiState.show
    }

    if (uiState is MapUiState.ChatUnreadMessages) {
        chatBitmap.value = if (uiState.count > 0) chatUnreadMessagesBitmap else chatDefaultBitmap
    }

    Box(
        modifier = Modifier
            .matchParentSize()
            .padding(top = padding.top.dp)
            .padding(bottom = padding.bottom.dp)
    ) {
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = 16.dp),
            onClick = { onAction(MapIntent.ChatTap(context)) },
            shape = CircleShape,
            containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
        ) {
            Image(
                bitmap = chatBitmap.value,
                contentDescription = null,
            )
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = 16.dp),
            onClick = { onAction(MapIntent.Search("")) },
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
            onClick = { },
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
            onClick = { moveMap(mapView, changeZoom = CHANGE_ZOOM) },
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
            onClick = { moveMap(mapView, changeZoom = -CHANGE_ZOOM) },
            shape = CircleShape,
            containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
        ) {
            Icon(painterResource(R.drawable.minus), "")
        }

        if (showQrScanButton) {
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
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-16).dp),
            onClick = {
                locationPermissionLauncher.runWithLocation(context) { lat, lon ->
                    moveMap(mapView, Point(lat ?: MOSCOW_LAT, lon ?: MOSCOW_LON))
                }
            },
            shape = CircleShape,
            containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
        ) {
            Icon(painterResource(R.drawable.to_user_position), "")
        }
    }
}
