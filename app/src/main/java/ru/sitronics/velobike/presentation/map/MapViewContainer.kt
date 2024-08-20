package ru.sitronics.velobike.presentation.map

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.ClusterListener
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolygonMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.sitronics.velobike.CLUSTERS_ZOOM
import ru.sitronics.velobike.INITIAL_ZOOM
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.tools.ClusterImageProvider
import ru.sitronics.velobike.tools.ClusterType
import ru.sitronics.velobike.tools.PinManager
import ru.sitronics.velobike.tools.PinType
import ru.sitronics.velobike.tools.RunWithLocation
import ru.sitronics.velobike.tools.drawText
import ru.sitronics.velobike.tools.getBitmapFromVectorDrawable
import ru.sitronics.velobike.tools.rememberLocationPermissionLauncher

@Composable
fun MapViewContainer(
    mapView: MapView,
    uiState: MapUiState,
    onAction: (MapIntent) -> Unit,
) {
    val context = LocalContext.current
//    val userLocationLayer = remember { MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow) }
    val cameraListener = remember {
        CameraListener { map, cameraPosition, _, finished ->
            if (finished) {
                println("!!!! Camera position changed")
                onAction(MapIntent.ChangeMapPosition(getMapRect(mapView), cameraPosition.zoom))
            }
        }
    }
    val tapListener = remember {
        MapObjectTapListener { mapObject, point ->
            val data = mapObject.userData as? MarkerUserData
            onAction(MapIntent.MapObjectTap(data))
            return@MapObjectTapListener true
        }
    }
    val userLocationObjectListener = remember {
        object : UserLocationObjectListener {
            override fun onObjectAdded(userLocationView: UserLocationView) {
//                userLocationLayer.setAnchor(
//                    PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()),
//                    PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat())
//                )

                userLocationView.arrow.setIcon(ImageProvider.fromResource(context, R.drawable.yandex_user_arrow))

                val pinIcon = userLocationView.pin.useCompositeIcon()

                pinIcon.setIcon(
                    "icon",
                    ImageProvider.fromResource(context, R.drawable.yandex_search),
                    IconStyle()/*.setAnchor(PointF(0f, 0f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(0f)
                        .setScale(1f)*/
                )

                pinIcon.setIcon(
                    "pin",
                    ImageProvider.fromResource(context, R.drawable.yandex_search_result),
                    IconStyle()/*.setAnchor(PointF(0.5f, 0.5f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(1f)
                        .setScale(0.5f)*/
                )

                userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001
            }
            override fun onObjectRemoved(p0: UserLocationView) {}
            override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}
        }
    }

    val locationPermissionLauncher = rememberLocationPermissionLauncher()
    locationPermissionLauncher.RunWithLocation { lat, lon ->
        // TODO: commented for debug purpose
        moveMap(mapView, Point(/*lat ?:*/ MOSCOW_LAT, /*lon ?:*/ MOSCOW_LON))
        MapKitFactory.getInstance().resetLocationManagerToDefault()
        try {
            MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow).apply {
                isVisible = true
                isHeadingEnabled = true
                setObjectListener(userLocationObjectListener)
            }
        } catch (ignored: Exception) {}
    }

    AndroidView({
        moveMap(mapView, Point(MOSCOW_LAT, MOSCOW_LON), initZoom = INITIAL_ZOOM)
        mapView.mapWindow.map.addCameraListener(cameraListener)
        mapView
    })

    val pinManager = remember { PinManager(context) }
    var isParkMode by remember { mutableStateOf(false) }
    val bikeClusterListener = remember { ClusterListener { cluster ->
            cluster.appearance.setIcon(ClusterImageProvider(cluster.size, mapView.mapWindow.map.cameraPosition.zoom, pinManager, isParkMode, ClusterType.BIKE))
    }}
    val stationClusterListener = remember { ClusterListener { cluster ->
        cluster.appearance.setIcon(ClusterImageProvider(cluster.size, mapView.mapWindow.map.cameraPosition.zoom, pinManager, isParkMode, ClusterType.STATION))
    }}
    val bikeClusterCollection = remember { mapView.mapWindow.map.mapObjects.addClusterizedPlacemarkCollection(bikeClusterListener) }
    val bikePlacemarks = remember { hashMapOf<String, PlacemarkMapObject>() }
    val stationClusterCollection = remember { mapView.mapWindow.map.mapObjects.addClusterizedPlacemarkCollection(stationClusterListener) }
    val stationPlacemarks = remember { hashMapOf<String, PlacemarkMapObject>() }
    val parkingCollection = remember { mapView.mapWindow.map.mapObjects.addCollection() }
    val parkingPlacemarks = remember { hashMapOf<String, PlacemarkMapObject>() }

    val slowZoneCollection = remember { mapView.mapWindow.map.mapObjects.addCollection() }
    val slowZonePolygons = remember { hashMapOf<Int, PolygonMapObject>() }
//    val slowZonePolygons = remember { mutableListOf<PolygonMapObject>() }
    val slowZoneMarkerCollection = remember { mapView.mapWindow.map.mapObjects.addCollection() }
    val slowZoneMarkerPlacemarks = remember { hashMapOf<Int, PlacemarkMapObject>() }
//    val slowZoneMarkerPlacemarks = remember { mutableListOf<PlacemarkMapObject>() }
    val moveZoneCollection = remember { mapView.mapWindow.map.mapObjects.addCollection() }
    val moveZonePolygons = remember { mutableListOf<PolygonMapObject>() }

    SideEffect {
        when(uiState) {
    /*
            is MapUiState.MapContent -> {
                updateBikes(LocalContext.current, uiState.bikes, bikeClusterCollection, bikePlacemarks, tapListener)
                updateStations(LocalContext.current, uiState.stations, stationClusterCollection, stationPlacemarks, tapListener)
                updateParkings(LocalContext.current, uiState.parkings, parkingCollection, parkingPlacemarks, tapListener)
                updateSlowZones(LocalContext.current, uiState.slowZones, uiState.showMarkers, slowZoneCollection, slowZonePolygons, slowZoneMarkerCollection, slowZoneMarkerPlacemarks, tapListener)
            }
    */
            is MapUiState.Bikes -> {
                updateBikes(uiState.bikes, bikeClusterCollection, bikePlacemarks, tapListener, pinManager)
            }
            is MapUiState.Parkings -> {
                val isParkModeChanged = isParkMode != uiState.isParkMode
                isParkMode = uiState.isParkMode
                updateStations(uiState.stations, stationClusterCollection, stationPlacemarks, tapListener, pinManager, isParkMode, isParkModeChanged)
                updateParkings(uiState.parkings, parkingCollection, parkingPlacemarks, tapListener, pinManager)
            }
            is MapUiState.SlowZones -> {
                updateSlowZones(context, uiState.slowZones, uiState.showMarkers, slowZoneCollection, slowZonePolygons, slowZoneMarkerCollection, slowZoneMarkerPlacemarks, tapListener)
            }
            is MapUiState.MoveZones -> {
                updateMoveZones(context, uiState.moveZone, moveZoneCollection, moveZonePolygons, tapListener)
            }
            else -> {}
        }
    }
}

private fun updateBikes(
    bikes: List<Bike>?,
    mapCollection: ClusterizedPlacemarkCollection,
    placemarks: HashMap<String, PlacemarkMapObject>,
    tapListener: MapObjectTapListener,
    pinManager: PinManager
) {
    if (bikes.isNullOrEmpty()) {
        mapCollection.clear()
        placemarks.clear()
        return
    }

    val currentIds = mutableListOf<String>()

    bikes.forEach { bike ->
        val id = bike.id
        currentIds.add(id)

        if (id !in placemarks) {
            placemarks[id] = mapCollection.addPlacemark().apply {
                geometry = Point(bike.latitude, bike.longitude)
                val bitmap = pinManager.getPinBitmap(PinType.BIKE, batteryPower = bike.batteryPower)
                setIcon(ImageProvider.fromBitmap(bitmap))
                userData = MarkerUserData.Bike(id)
                addTapListener(tapListener)
            }
        }
    }

    val removedIds = placemarks.keys.filter { !currentIds.contains(it) }
    removedIds.forEach { key ->
        placemarks.remove(key)?.let {
            mapCollection.remove(it)
        }
    }

    mapCollection.clusterPlacemarks(60.0, CLUSTERS_ZOOM)
}

private fun updateStations(
    parkings: List<Parking>?,
    mapCollection: ClusterizedPlacemarkCollection,
    placemarks: HashMap<String, PlacemarkMapObject>,
    tapListener: MapObjectTapListener,
    pinManager: PinManager,
    isParkMode: Boolean,
    isParkModeChanged: Boolean,
) {
    if (parkings.isNullOrEmpty() || isParkModeChanged) {
        mapCollection.clear()
        placemarks.clear()
    }

    val currentIds = mutableListOf<String>()

    parkings?.forEach { parking ->
        val id = parking.id
        currentIds.add(id)

        if (id !in placemarks) {
            placemarks[id] = mapCollection.addPlacemark().apply {
                geometry = Point(parking.latitude, parking.longitude)
                val bitmap = pinManager.getPinBitmap(
                    if (!isParkMode) PinType.STATION else PinType.STATION_PARK,
                    parking.availableElectricBikes,
                    parking.availableNonElectricBikes,
                )

                setIcon(ImageProvider.fromBitmap(bitmap))
                userData = MarkerUserData.Station(id)
                addTapListener(tapListener)
            }
        }
    }

    val removedIds = placemarks.keys.filter { !currentIds.contains(it) }
    removedIds.forEach { key ->
        placemarks.remove(key)?.let {
            mapCollection.remove(it)
        }
    }

    mapCollection.clusterPlacemarks(60.0, CLUSTERS_ZOOM)
}

private fun updateParkings(
    parkings: List<Parking>?,
    mapCollection: MapObjectCollection,
    placemarks: HashMap<String, PlacemarkMapObject>,
    tapListener: MapObjectTapListener,
    pinManager: PinManager
) {
    if (parkings.isNullOrEmpty()) {
        mapCollection.clear()
        placemarks.clear()
        return
    }

    val currentIds = mutableListOf<String>()
    val bitmap = pinManager.getPinBitmap(PinType.PARKING)
    val imageProvider = ImageProvider.fromBitmap(bitmap)

    parkings.forEach { parking ->
        val id = parking.id
        currentIds.add(id)

        if (id !in placemarks) {
            placemarks[id] = mapCollection.addPlacemark().apply {
                geometry = Point(parking.latitude, parking.longitude)
                setIcon(imageProvider)
                userData = MarkerUserData.Parking(id)
                addTapListener(tapListener)
            }
        }
    }

    val removedIds = placemarks.keys.filter { !currentIds.contains(it) }
    removedIds.forEach { key ->
        placemarks.remove(key)?.let {
            mapCollection.remove(it)
        }
    }
}

private fun updateSlowZones(
    context: Context,
    zones: List<SlowZoneObject>?,
    showMarkers: Boolean,
    zoneCollection: MapObjectCollection,
    polygons: HashMap<Int, PolygonMapObject>,
    markerCollection: MapObjectCollection,
    markerPlacemarks: HashMap<Int, PlacemarkMapObject>,
    tapListener: MapObjectTapListener,
) {
    if (zones.isNullOrEmpty()) {
        zoneCollection.clear()
        polygons.clear()
        markerCollection.clear()
        markerPlacemarks.clear()
        return
    }

    val currentIds = mutableListOf<Int>()
    val slowZoneBitmap = context.getBitmapFromVectorDrawable(R.drawable.slow_zone)
    val slowZoneTimeBitmap = context.getBitmapFromVectorDrawable(R.drawable.slow_zone_time)

    zones.forEach { zone ->
        val id = zone.id
        currentIds.add(id)

        if (id !in polygons) {
            polygons[id] = zoneCollection.addPolygon(zone.polygon).apply {
                zIndex = 1f
                strokeWidth = 1f
                strokeColor = ContextCompat.getColor(context, R.color.slow_zone_stroke)
                fillColor = ContextCompat.getColor(context, R.color.slow_zone_fillcolor)
                isDraggable = false
                userData = MarkerUserData.SlowZone(zone.id)
                addTapListener(tapListener)
            }

            val text = context.getString(R.string.speed_limit, zone.speedLimit)
            val scheduled = zone.startTime > 0
            val bitmap = (if (scheduled) slowZoneTimeBitmap else slowZoneBitmap)
                .drawText(context, text, TEXT_SIZE, Color.BLACK, scheduled)

            markerPlacemarks[id] = markerCollection.addPlacemark().apply {
                geometry = zone.markerPoint
                setIcon(ImageProvider.fromBitmap(bitmap))
            }
        }
        markerPlacemarks[id]?.isVisible = showMarkers
    }

    val removedIds = polygons.keys.filter { !currentIds.contains(it) }
    removedIds.forEach { key ->
        polygons.remove(key)?.let {
            zoneCollection.remove(it)
        }
        markerPlacemarks.remove(key)?.let {
            markerCollection.remove(it)
        }
    }
}

/*
private fun updateSlowZones(
    context: Context,
    objects: List<SlowZoneObject>?,
    showMarkers: Boolean,
    zoneCollection: MapObjectCollection,
    polygons: MutableList<PolygonMapObject>,
    markerCollection: MapObjectCollection,
    markerPlacemarks: MutableList<PlacemarkMapObject>,
    tapListener: MapObjectTapListener,
) {
    if (objects.isNullOrEmpty() || polygons.size == objects.size) {
        val visible = !objects.isNullOrEmpty()
        polygons.forEach { it.isVisible = visible }
        markerPlacemarks.forEach { it.isVisible = showMarkers }
    } else {
        zoneCollection.clear()
        polygons.clear()
        markerCollection.clear()
        markerPlacemarks.clear()

        val slowZoneBitmap = context.getBitmapFromVectorDrawable(R.drawable.slow_zone)
        val slowZoneTimeBitmap = context.getBitmapFromVectorDrawable(R.drawable.slow_zone_time)

        objects.forEach { obj ->
            zoneCollection.addPolygon(obj.polygon).apply {
                zIndex = 1f
                strokeWidth = 1f
                strokeColor = ContextCompat.getColor(context, R.color.slow_zone_stroke)
                fillColor = ContextCompat.getColor(context, R.color.slow_zone_fillcolor)
                isDraggable = false
                userData = MarkerUserData.SlowZone(obj.id)
                addTapListener(tapListener)
                polygons.add(this)
            }

            val text = context.getString(R.string.speed_limit, obj.speedLimit)
            val scheduled = obj.startTime > 0
            val bitmap = (if (scheduled) slowZoneTimeBitmap else slowZoneBitmap)
                .drawText(context, text, TEXT_SIZE, Color.BLACK, scheduled)

            markerCollection.addPlacemark().apply {
                geometry = obj.markerPoint
                setIcon(ImageProvider.fromBitmap(bitmap))
                markerPlacemarks.add(this)
            }
        }
    }
}
*/

private fun updateMoveZones(
    context: Context,
    obj: MoveZoneObject,
    zoneCollection: MapObjectCollection,
    polygons: MutableList<PolygonMapObject>,
    tapListener: MapObjectTapListener,
) {
    zoneCollection.clear()
    polygons.clear()

    zoneCollection.addPolygon(obj.polygon).apply {
        zIndex = 1f
        strokeWidth = 1f
        strokeColor = ContextCompat.getColor(context, R.color.move_zone_stroke)
        fillColor = ContextCompat.getColor(context, R.color.move_zone_fillcolor)
        isDraggable = false
        userData = MarkerUserData.MoveZone(obj.id)
        addTapListener(tapListener)
        polygons.add(this)
    }
}

/**
 * Remembers a MapView and gives it the lifecycle of the current LifecycleOwner
 */
@Composable
fun rememberMapViewWithLifecycle(onAction: (MapIntent) -> Unit) : MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(key1 = lifecycle, key2 = mapView) {
        // Make MapView follow the current lifecycle
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> onMapStart(mapView, onAction)
                Lifecycle.Event.ON_STOP -> onMapStop(mapView, onAction)
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            onMapStop(mapView, onAction)
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

private fun onMapStart(mapView: MapView, onAction: (MapIntent) -> Unit) {
    MapKitFactory.getInstance().onStart()
    mapView.onStart()
    onAction(MapIntent.MapStart)
}

private fun onMapStop(mapView: MapView, onAction: (MapIntent) -> Unit) {
    mapView.onStop()
    MapKitFactory.getInstance().onStop()
    onAction(MapIntent.MapStop)
}

private fun getMapRect(mapView: MapView) : MapRect {
    val width = mapView.mapWindow.width().toFloat()
    val height = mapView.mapWindow.height().toFloat()
    val topLeft = mapView.mapWindow.screenToWorld(ScreenPoint(0f, 0f)) ?: Point(0.0, 0.0)
    val bottomRight = mapView.mapWindow.screenToWorld(ScreenPoint(width, height)) ?: Point(0.0, 0.0)
    return MapRect(bottomRight.latitude, topLeft.longitude, topLeft.latitude, bottomRight.longitude)
}

fun moveMap(mapView: MapView, point: Point? = null, changeZoom: Float = 0f, initZoom: Float? = null) {
    with(mapView.mapWindow.map) {
        move(CameraPosition(
            point ?: cameraPosition.target,
            initZoom ?: (cameraPosition.zoom + changeZoom),
            cameraPosition.azimuth,
            cameraPosition.tilt
        ))
    }
}
