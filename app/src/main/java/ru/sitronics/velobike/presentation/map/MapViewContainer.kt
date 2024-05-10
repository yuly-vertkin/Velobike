package ru.sitronics.velobike.presentation.map

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolygonMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.sitronics.velobike.INITIAL_ZOOM
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.tools.ClusterImageProvider
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
                userLocationView.arrow.setIcon(ImageProvider.fromResource(context, R.drawable.user_place))
            }
            override fun onObjectRemoved(p0: UserLocationView) {}
            override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}
        }
    }

    val locationPermissionLauncher = rememberLocationPermissionLauncher()
    locationPermissionLauncher.RunWithLocation { lat, lon ->
        // TODO: commented for debug purpose
        moveMap(mapView, Point(MOSCOW_LAT, MOSCOW_LON) /*Point(lat, lon)*/)
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

    val bikeClusterListener = remember { ClusterListener { cluster ->
            cluster.appearance.setIcon(ClusterImageProvider(context, cluster.size, R.drawable.bike_cluster))
    }}
    val stationClusterListener = remember { ClusterListener { cluster ->
        cluster.appearance.setIcon(ClusterImageProvider(context, cluster.size, R.drawable.parking_cluster, Color.WHITE))
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
            updateBikes(LocalContext.current, uiState.bikes, bikeClusterCollection, bikePlacemarks, tapListener)
        }
        is MapUiState.Parkings -> {
            updateStations(LocalContext.current, uiState.stations, stationClusterCollection, stationPlacemarks, tapListener)
            updateParkings(LocalContext.current, uiState.parkings, parkingCollection, parkingPlacemarks, tapListener)
        }
        is MapUiState.SlowZones -> {
            updateSlowZones(LocalContext.current, uiState.slowZones, uiState.showMarkers, slowZoneCollection, slowZonePolygons, slowZoneMarkerCollection, slowZoneMarkerPlacemarks, tapListener)
        }
        is MapUiState.MoveZones -> {
            updateMoveZones(LocalContext.current, uiState.moveZone, moveZoneCollection, moveZonePolygons, tapListener)
        }
        else -> {}
    }
}

private fun updateBikes(
    context: Context,
    bikes: List<Bike>?,
    mapCollection: ClusterizedPlacemarkCollection,
    placemarks: HashMap<String, PlacemarkMapObject>,
    tapListener: MapObjectTapListener,
) {
    if (bikes.isNullOrEmpty()) {
        mapCollection.clear()
        placemarks.clear()
        return
    }

    val currentIds = mutableListOf<String>()
    val bitmap = context.getBitmapFromVectorDrawable(R.drawable.bike)
    val imageProvider = ImageProvider.fromBitmap(bitmap)

    bikes.forEach { bike ->
        val id = bike.id
        currentIds.add(id)

        if (id !in placemarks) {
            placemarks[id] = mapCollection.addPlacemark().apply {
                geometry = Point(bike.latitude, bike.longitude)
                setIcon(imageProvider)
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

    mapCollection.clusterPlacemarks(60.0, 15)
}

private fun updateStations(
    context: Context,
    parkings: List<Parking>?,
    mapCollection: ClusterizedPlacemarkCollection,
    placemarks: HashMap<String, PlacemarkMapObject>,
    tapListener: MapObjectTapListener,
) {
    if (parkings.isNullOrEmpty()) {
        mapCollection.clear()
        placemarks.clear()
        return
    }

    val currentIds = mutableListOf<String>()
    val stationBitmap = context.getBitmapFromVectorDrawable(R.drawable.station)
    val stationElectroBitmap = context.getBitmapFromVectorDrawable(R.drawable.station_electro)

    parkings.forEach { parking ->
        val id = parking.id
        currentIds.add(id)

        if (id !in placemarks) {
            placemarks[id] = mapCollection.addPlacemark().apply {
                geometry = Point(parking.latitude, parking.longitude)
                val text = (parking.availableNonElectricBikes + parking.availableElectricBikes).toString()
                val bitmap = if (parking.type.isElectro()) stationElectroBitmap.drawText(context, text, TEXT_SIZE, textOnRight = true)
                             else stationBitmap.drawText(context, text, TEXT_SIZE)
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

    mapCollection.clusterPlacemarks(60.0, 15)
}

private fun updateParkings(
    context: Context,
    parkings: List<Parking>?,
    mapCollection: MapObjectCollection,
    placemarks: HashMap<String, PlacemarkMapObject>,
    tapListener: MapObjectTapListener,
) {
    if (parkings.isNullOrEmpty()) {
        mapCollection.clear()
        placemarks.clear()
        return
    }

    val currentIds = mutableListOf<String>()
    val bitmap = context.getBitmapFromVectorDrawable(R.drawable.parking)
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
fun rememberMapViewWithLifecycle(onAction: (MapIntent) -> Unit): MapView {
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
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView.onStart()
                    onAction(MapIntent.MapStart)
                }
                Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
                    onAction(MapIntent.MapStop)
                }
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
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
