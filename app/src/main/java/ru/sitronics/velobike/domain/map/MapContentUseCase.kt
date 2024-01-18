package ru.sitronics.velobike.domain.map

import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.presentation.BaseUseCase
import ru.sitronics.velobike.tools.Logg
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapContentUseCase @Inject constructor(
    private val mapContentRepository: MapContentRepository,
    appContextProvider: AppContextProvider,
) : BaseUseCase(appContextProvider) {
//    private var showSlowZones: Boolean = false
//    private var showSlowZoneMarkers: Boolean = false

    fun getBike(id: String) : Bike? =
        mapContentRepository.getData().bikes?.find { it.id == id }

    fun runWithBike(id: String, action: (Bike) -> Unit) {
        Logg.d("!!!! runWithBike: $id")
        val bike = getBike(id)
        if (bike != null) {
            Logg.d("!!!! found Bike: $id")
            action(bike)
        } else {
            Logg.d("!!!! runWithBike processNetworkCall $id")
            processNetworkCall(
                action = { mapContentRepository.getBike(id) },
                onSuccess = {
                    Logg.d("!!!! getBike ${it.id}")
                    action(it)
                },
                onError = { Logg.d("!!!! ERROR getBike") },
            )
        }
    }

    fun getStation(id: String) : Parking? =
        mapContentRepository.getData().stations?.find { it.id == id }

    fun getParking(id: String) : Parking? =
        mapContentRepository.getData().parkings?.find { it.id == id }

    fun updateBikes(
        mapRect: MapRect, zoom: Float,
        onSuccess: (List<Bike>) -> Unit, onError: (String?) -> Unit
    ) {
        if (zoom < SHOW_CONTENT_ZOOM) return

        processNetworkCall(
            action = { mapContentRepository.getBikes(mapRect) },
            onSuccess = { bikes ->
                Logg.d("!!! getBikes() ${bikes.size}")
                mapContentRepository.saveData(mapContentRepository.getData().copy(
                    bikes = bikes
                ))
                onSuccess(bikes)
            },
            onError = {
                Logg.d("!!! ERROR getBikes()")
                onError(null)
            },
            force = true,
            callName = "getBikes"
        )
    }

    fun updateParkings(
        mapRect: MapRect, zoom: Float,
        onSuccess: (List<Parking>, List<Parking>) -> Unit, onError: (String?) -> Unit
    ) {
        if (zoom < SHOW_CONTENT_ZOOM) return

        processNetworkCall(
            action = { mapContentRepository.getParkings(mapRect) },
            onSuccess = { items ->
                val stations = items.filter { it.type.isStation() }
                val parkings = items.filter { it.type.isParking() }
                Logg.d("!!! getParkings() stations: ${stations.size}, parkings: ${parkings.size}")
                mapContentRepository.saveData(mapContentRepository.getData().copy(
                    stations = stations,
                    parkings = parkings,
                ))
                onSuccess(stations, if (zoom >= SHOW_PARKINGS_ZOOM) parkings else emptyList())
            },
            onError = {
                Logg.d("!!! ERROR getParkings() ${it.message}")
                onError(null)
            },
            force = true,
            callName = "getParkings"
        )
    }

    fun updateSlowZones(
        zoom: Float,
        onSuccess: (List<SlowZone>, Boolean) -> Unit, onError: (String?) -> Unit
    ) {
        val show = zoom >= SHOW_SLOW_ZONE_ZOOM
        val showMarkers = zoom >= SHOW_SLOW_ZONE_MARKER_ZOOM

        if (show && mapContentRepository.getData().slowZones == null) {
            processNetworkCall(
                action = { mapContentRepository.getSlowZones() },
                onSuccess = {
                    Logg.d("!!! getSlowZones() ${it.size}")
                    mapContentRepository.saveData(
                        mapContentRepository.getData().copy(
                            slowZones = it
                        )
                    )
                    onSuccess(it, showMarkers)
                },
                onError = {
                    Logg.d("!!! ERROR getSlowZones()")
                    onError(null)
                },
                callName = "getSlowZones"
            )
        } else /*if (show != showSlowZones || showMarkers != showSlowZoneMarkers)*/ {
            mapContentRepository.getData().slowZones?.let {
                onSuccess(if (show) it else emptyList(), showMarkers)
            }
        }
//        showSlowZones = show
//        showSlowZoneMarkers = showMarkers
    }

    fun updateMoveZones(
        onSuccess: (List<MoveZone>) -> Unit, onError: (String?) -> Unit
    ) {
        if (mapContentRepository.getData().moveZones == null) {
            processNetworkCall(
                action = { mapContentRepository.getMoveZones() },
                onSuccess = {
                    Logg.d("!!! getMoveZones() ${it.size}")
                    mapContentRepository.saveData(
                        mapContentRepository.getData().copy(
                            moveZones = it
                        )
                    )
                    onSuccess(it)
                },
                onError = {
                    Logg.d("!!! ERROR getMoveZones()")
                    onError(null)
                },
                callName = "getMoveZones"
            )
        } else {
            mapContentRepository.getData().moveZones?.let {
                onSuccess(it)
            }
        }
    }

    companion object {
        private const val SHOW_CONTENT_ZOOM = 5f
        private const val SHOW_PARKINGS_ZOOM = 16f
        private const val SHOW_SLOW_ZONE_ZOOM = 11f
        private const val SHOW_SLOW_ZONE_MARKER_ZOOM = 14f
    }
}