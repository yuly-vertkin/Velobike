package ru.sitronics.velobike.presentation.map

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView
import ru.sitronics.velobike.R
import ru.sitronics.velobike.ZOOM_STEP
import ru.sitronics.velobike.tools.pxToDp
import ru.sitronics.velobike.tools.rememberLocationPermissionLauncher
import ru.sitronics.velobike.tools.runWithLocation
import ru.sitronics.velobike.ui.theme.ButtonBackgroundColor
import ru.sitronics.velobike.ui.theme.LightGrayFilterColor
import ru.sitronics.velobike.ui.theme.MapBackgroundColor
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
//    val chatDefaultBitmap = remember { context.getBitmapFromVectorDrawable(R.drawable.chat).asImageBitmap() }
//    val chatUnreadMessagesBitmap = remember { context.getBitmapFromVectorDrawable(R.drawable.chat).drawChatBitmap().asImageBitmap() }
//    val chatBitmap = remember { mutableStateOf(chatDefaultBitmap) }
    var height by remember { mutableStateOf(0) }
    var selectedFilter by remember { mutableStateOf(0) }
    var isParkMode by remember { mutableStateOf(false) }
    val zoom = mapView.mapWindow.map.cameraPosition.zoom

    when (uiState) {
        is MapUiState.QrScanButton -> showQrScanButton = uiState.show
//        is MapUiState.ChatUnreadMessages -> chatBitmap.value =
//            if (uiState.count > 0) chatUnreadMessagesBitmap else chatDefaultBitmap
        is MapUiState.Parkings -> isParkMode = uiState.isParkMode
        else -> {}
    }

    Box(
        modifier = Modifier
            .matchParentSize()
            .padding(top = padding.top.pxToDp(context).dp)
            .padding(bottom = padding.bottom.pxToDp(context).dp)
            .background(MapBackgroundColor)
            .onGloballyPositioned { coordinates ->
                height = coordinates.size.height.pxToDp(context)
            }

    ) {
        // top part
        SmallFloatingActionButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp)
                .padding(top = 24.dp),
            onClick = { onAction(MapIntent.ChatTap(context)) },
            shape = CircleShape,
            containerColor = Color.White,
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(6.dp),
        ) {
//            Image(bitmap = chatBitmap.value, contentDescription = null)
            Icon(painterResource(R.drawable.menu), null, Modifier.padding(all = 12.dp))
        }

        SmallFloatingActionButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp)
                .padding(top = 24.dp),
            onClick = { onAction(MapIntent.Search("")) },
            shape = CircleShape,
            containerColor = Color.White,
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(6.dp),
        ) {
            Icon(painterResource(R.drawable.search), "", Modifier.padding(all = 12.dp))
        }

        // middle part
        if (height >= MIN_HEIGHT_FOR_ZOOM_BUTTONS) {
            Column(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .padding(bottom = 116.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(48.dp),
                ) {
                    SmallFloatingActionButton(
                        onClick = { if (isParkMode) onAction(MapIntent.ChangeParkMode(zoom)) },
                        shape = CircleShape,
                        containerColor = if (!isParkMode) SelectedBackgroundColor else Color.White,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    ) {
                        Icon(
                            painterResource(R.drawable.pin_bicycle),
                            "",
                            Modifier.padding(all = 12.dp),
                            tint = if (!isParkMode) Color.White else LightGrayFilterColor
                        )
                    }

                    SmallFloatingActionButton(
                        onClick = { if (!isParkMode) onAction(MapIntent.ChangeParkMode(zoom)) },
                        shape = CircleShape,
                        containerColor = if (isParkMode) SelectedBackgroundColor else Color.White,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    ) {
                        Icon(
                            painterResource(R.drawable.pin_parking),
                            "",
                            Modifier.padding(all = 12.dp),
                            tint = if (isParkMode) Color.White else LightGrayFilterColor
                        )
                    }
                }

                SmallFloatingActionButton(
                    modifier = Modifier.padding(top = 12.dp),
                    onClick = { moveMap(mapView, changeZoom = ZOOM_STEP) },
                    shape = RoundedCornerShape(48.dp, 48.dp),
                    containerColor = Color.White,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(6.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.plus),
                        "",
                        Modifier.padding(all = 12.dp)/*.padding(bottom = 16.dp)*/
                    )
                }

                SmallFloatingActionButton(
                    onClick = { moveMap(mapView, changeZoom = -ZOOM_STEP) },
                    shape = RoundedCornerShape(0.dp, 0.dp, 48.dp, 48.dp),
                    containerColor = Color.White,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(6.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.minus),
                        "",
                        Modifier.padding(all = 12.dp)/*.padding(top = 16.dp)*/
                    )
                }
            }
        }

        // bottom part
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
        ) {
            SmallFloatingActionButton(
                modifier = Modifier.align(Alignment.End).padding(end = 8.dp),
                onClick = {
                    locationPermissionLauncher.runWithLocation(context) { lat, lon ->
                        moveMap(mapView, Point(lat ?: MOSCOW_LAT, lon ?: MOSCOW_LON))
                    }
                },
                shape = CircleShape,
                containerColor = Color.White,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(6.dp),
            ) {
                Icon(painterResource(R.drawable.location), "", Modifier.padding(all = 12.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    // used instead .clickable { } to avoid click visual effects
                    .pointerInput(null) { detectTapGestures { } }
            ) {
                SmallFloatingActionButton(
                    modifier = Modifier.padding(start = 16.dp)/*.padding(vertical = 16.dp)*/,
                    onClick = { },
                    shape = CircleShape,
                    containerColor = Color.White,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(6.dp),
                ) {
                    Icon(painterResource(R.drawable.filter_settings), "", Modifier.padding(all = 12.dp))
                }

                @DrawableRes var resId = if (!isParkMode) R.drawable.filter_bike else R.drawable.filter_park
                @StringRes var descId = R.string.filter_no_station
                FilterCard(resId, R.string.filter_bike, descId, isParkMode, selectedFilter == 1) {
                    selectedFilter = if (selectedFilter != 1) 1 else 0
                    onAction(MapIntent.MapFilterTap(
                        if (selectedFilter != 0) BikeParkingType.ELECTRO_2_0 else BikeParkingType.ALL,
                        zoom
                    ))
                }
                resId = if (!isParkMode) R.drawable.filter_bike_m else R.drawable.filter_park_m
                descId = if (!isParkMode) R.string.filter_at_station else R.string.filter_free_places
                FilterCard(resId, R.string.filter_bike_m, descId, isParkMode, selectedFilter == 2) {
                    selectedFilter = if (selectedFilter != 2) 2 else 0
                    onAction(MapIntent.MapFilterTap(
                        if (selectedFilter != 0) BikeParkingType.MECHANICAL else BikeParkingType.ALL,
                        zoom
                    ))
                }
                resId = if (!isParkMode) R.drawable.filter_bike_el else R.drawable.filter_park_el
                descId = if (!isParkMode) R.string.filter_at_station else R.string.filter_free_places
                FilterCard(resId, R.string.filter_bike_el, descId, isParkMode, selectedFilter == 3) {
                    selectedFilter = if (selectedFilter != 3) 3 else 0
                    onAction(MapIntent.MapFilterTap(
                        if (selectedFilter != 0) BikeParkingType.ELECTRICAL else BikeParkingType.ALL,
                        zoom
                    ))
                }
            }

            if (showQrScanButton) {
                Button(
                    onClick = { onAction(MapIntent.ScanQrTap) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonBackgroundColor),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.qr_code),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.qr_code),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterCard(
    @DrawableRes resId: Int, @StringRes textId: Int, @StringRes descId: Int,
    isParkMode: Boolean, isSelected: Boolean, onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isSelected) SelectedBackgroundColor else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(if (!isParkMode) 48.dp else 16.dp),
        modifier = Modifier
            .padding(vertical = 12.dp)
            .padding(start = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(id = resId), null, Modifier.padding(start = 8.dp))

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
                    color = LightGrayFilterColor,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .offset(y = (-2).dp)
                        .padding(bottom = 2.dp)
                )
            }
        }
    }
}
