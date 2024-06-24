package ru.sitronics.velobike.presentation.map

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView
import ru.sitronics.velobike.R
import ru.sitronics.velobike.ZOOM_STEP
import ru.sitronics.velobike.tools.drawChatBitmap
import ru.sitronics.velobike.tools.getBitmapFromVectorDrawable
import ru.sitronics.velobike.tools.pxToDp
import ru.sitronics.velobike.tools.rememberLocationPermissionLauncher
import ru.sitronics.velobike.tools.runWithLocation
import ru.sitronics.velobike.ui.theme.LightGrayTextColor
import ru.sitronics.velobike.ui.theme.SelectedBackgroundColor

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
    var height by remember { mutableStateOf(0) }
    var selectedFilter by remember { mutableStateOf(0) }
    val zoom = mapView.mapWindow.map.cameraPosition.zoom

    if (uiState is MapUiState.QrScanButton) {
        showQrScanButton = uiState.show
    }

    if (uiState is MapUiState.ChatUnreadMessages) {
        chatBitmap.value = if (uiState.count > 0) chatUnreadMessagesBitmap else chatDefaultBitmap
    }

    Box(
        modifier = Modifier
            .matchParentSize()
            .padding(top = padding.top.pxToDp(context).dp)
            .padding(bottom = padding.bottom.pxToDp(context).dp)
            .onGloballyPositioned { coordinates ->
                height = coordinates.size.height.pxToDp(context)
            }

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

        if (height >= MIN_HEIGHT_FOR_ZOOM_BUTTONS) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-8).dp, y = (-32).dp),
                onClick = { moveMap(mapView, changeZoom = ZOOM_STEP) },
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
                onClick = { moveMap(mapView, changeZoom = -ZOOM_STEP) },
                shape = CircleShape,
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
            ) {
                Icon(painterResource(R.drawable.minus), "")
            }
        }

        if (showQrScanButton) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-16).dp),
                onClick = { onAction(MapIntent.ScanQrTap) },
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(bottom = 80.dp)
                .horizontalScroll(rememberScrollState())
                // used instead .clickable { } to avoid click visual effects
                .pointerInput(null) { detectTapGestures { } }
        ) {
            Image(
                painter = painterResource(id = R.drawable.filter_settings),
                contentDescription = null,
                contentScale = ContentScale.None,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .padding(vertical = 16.dp)
                    .padding(top = 6.dp)
                    .clickable { onAction(MapIntent.ChangeParkMode) }
            )

            FilterCard(R.drawable.filter_bike, R.string.filter_bike, R.string.filter_no_station, selectedFilter == 1) {
                selectedFilter = if (selectedFilter != 1) 1 else 0
                onAction(MapIntent.MapFilterTap(BikeParkingType.ELECTRO_2_0, zoom))
            }
            FilterCard(R.drawable.filter_bike_m, R.string.filter_bike_m, R.string.filter_at_station, selectedFilter == 2) {
                selectedFilter = if (selectedFilter != 2) 2 else 0
                onAction(MapIntent.MapFilterTap(BikeParkingType.MECHANICAL, zoom))
            }
            FilterCard(R.drawable.filter_bike_el, R.string.filter_bike_el, R.string.filter_at_station, selectedFilter == 3) {
                selectedFilter = if (selectedFilter != 3) 3 else 0
                onAction(MapIntent.MapFilterTap(BikeParkingType.ELECTRICAL, zoom))
            }
        }
    }
}

@Composable
private fun FilterCard(@DrawableRes resId: Int, @StringRes textId: Int, @StringRes descId: Int, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isSelected) SelectedBackgroundColor else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(48.dp),
        modifier = Modifier
            .padding(start = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp)
            )

            Column (
                modifier = Modifier
                    .padding(start = 8.dp)
                    .padding(end = 16.dp)
                    .padding(top = 4.dp)
            ) {
                Text(
                    text = stringResource(textId),
                    color = if (isSelected) Color.White else Color.Black
                )
                Text(
                    text = stringResource(descId),
                    color = LightGrayTextColor,
                    fontSize = 12.sp,
                    modifier = Modifier.offset(y = (-6).dp)
                )
            }
        }
    }
}
