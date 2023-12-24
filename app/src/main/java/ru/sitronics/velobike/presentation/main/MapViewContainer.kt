package ru.sitronics.velobike.presentation.main

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.ClusterListener
import com.yandex.mapkit.map.ClusterizedPlacemarkCollection
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import ru.sitronics.velobike.INITIAL_ZOOM
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.tools.ClusterImageProvider
import ru.sitronics.velobike.tools.Logg
import ru.sitronics.velobike.tools.RunWithLocation
import ru.sitronics.velobike.tools.getBitmapFromVectorDrawable
import ru.sitronics.velobike.tools.rememberLocationPermissionLauncher

const val MOSCOW_LAT = 55.75222
const val MOSCOW_LON = 37.61556

data class Marker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
)

@Composable
fun MapViewContainer(
    uiState: MainUiState,
    onAction: (MainIntent) -> Unit,
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
//    val coroutineScope = rememberCoroutineScope()
    val cameraListener = remember {
        CameraListener { map, cameraPosition, _, finished ->
            if (finished) {
                println("!!! Camera position changed")
                onAction(MainIntent.ChangeMapPosition(getMapRect(mapView), cameraPosition.zoom))
            }
        }
    }

    val locationPermissionLauncher = rememberLocationPermissionLauncher()
    locationPermissionLauncher.RunWithLocation { lat, lon ->
        // TODO: commented for debug purpose
        moveMap(mapView, /*lat ?:*/ MOSCOW_LAT, /*lon ?:*/ MOSCOW_LON)
    }

    Logg.d("!!! MapViewContainer called")

    AndroidView({
        moveMap(mapView, MOSCOW_LAT, MOSCOW_LON)
        mapView.mapWindow.map.addCameraListener(cameraListener)
        mapView
    })

    val bikeClusterListener = remember { ClusterListener { cluster ->
            cluster.appearance.setIcon(ClusterImageProvider(context, cluster.size, R.drawable.bike_cluster))
    }}
    val parkingClusterListener = remember { ClusterListener { cluster ->
        cluster.appearance.setIcon(ClusterImageProvider(context, cluster.size, R.drawable.parking_cluster, Color.WHITE))
    }}
//    val bikeCollection = remember { mapView.mapWindow.map.mapObjects.addCollection() }
    val bikeClusterCollection = remember { mapView.mapWindow.map.mapObjects.addClusterizedPlacemarkCollection(bikeClusterListener) }
    val bikePlacemarks = remember { hashMapOf<String, PlacemarkMapObject>() }
//    val parkingCollection = remember { mapView.mapWindow.map.mapObjects.addCollection() }
    val parkingClusterCollection = remember { mapView.mapWindow.map.mapObjects.addClusterizedPlacemarkCollection(parkingClusterListener) }
    val parkingPlacemarks = remember { hashMapOf<String, PlacemarkMapObject>() }

    when(uiState) {
        is MainUiState.BikesUpdated -> {
            val markers = uiState.bikes.map { Marker(it.deviceId, it.latitude, it.longitude) }
            updateMarkers(LocalContext.current, markers, bikeClusterCollection, bikePlacemarks, R.drawable.bike, "bike")
        }
        is MainUiState.ParkingsUpdated -> {
            val markers = uiState.parkings.map { Marker(it.id, it.latitude, it.longitude) }
            updateMarkers(LocalContext.current, markers, parkingClusterCollection, parkingPlacemarks, R.drawable.parking, "parking")
        }
        else -> {}
    }
}

private fun updateMarkers(
    context: Context,
    markers: List<Marker>,
//    pinsCollection: MapObjectCollection,
    clusterizedCollection: ClusterizedPlacemarkCollection,
    placemarks: HashMap<String, PlacemarkMapObject>,
    resourceId: Int,
    name: String,
) {
    val currentIds = mutableListOf<String>()
    val bitmap = context.getBitmapFromVectorDrawable(resourceId)
    val imageProvider = ImageProvider.fromBitmap(bitmap)

    Logg.d("!!! update $name: ${markers.size}")
    markers.forEach { marker ->
        val id = marker.id
        currentIds.add(id)

        if (id !in placemarks) {
            placemarks[id] = /*pinsCollection*/clusterizedCollection.addPlacemark().apply {
                geometry = Point(marker.latitude, marker.longitude)
                setIcon(imageProvider)
            }
        }
    }

    val removedIds = placemarks.keys.filter { !currentIds.contains(it) }
    removedIds.forEach { key ->
        placemarks.remove(key)?.let {
            /*pinsCollection*/clusterizedCollection.remove(it)
            Logg.d("!!! removed!")
        }
    }

    clusterizedCollection.clusterPlacemarks(60.0, 15)
}

/**
 * Remembers a MapView and gives it the lifecycle of the current LifecycleOwner
 */
@Composable
fun rememberMapViewWithLifecycle(): MapView {
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
                }
                Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
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

private fun moveMap(mapView: MapView, lat: Double, lon: Double, zoom: Float = INITIAL_ZOOM) {
    with(mapView.mapWindow.map) {
        move(CameraPosition(
            Point(lat, lon),
            zoom,
            cameraPosition.azimuth,
            cameraPosition.tilt
        ))
    }
}