package ru.sitronics.velobike.data.repositories.map

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.data.network.MapContentService
import ru.sitronics.velobike.data.repositories.BaseRepository
import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.BikeInventoryStatus
import ru.sitronics.velobike.domain.map.BikeOperativeStatus
import ru.sitronics.velobike.domain.map.MapContentData
import ru.sitronics.velobike.domain.map.MapContentRepository
import ru.sitronics.velobike.domain.map.MoveZone
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.domain.map.SlowZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapContentRepositoryImp @Inject constructor(
    private val service: MapContentService,
    appContextProvider: AppContextProvider,
    gson: Gson,
) : BaseRepository<MapContentData>(appContextProvider, gson), MapContentRepository {

    override fun getData() : MapContentData =
        super.getData() ?: run {
            val data = MapContentData()
            saveData(data)
            data
        }

    override fun saveData(data: MapContentData) {
        super.saveData(data)
    }

    override fun getBikes(mapRect: MapRect) : Flow<Result<List<Bike>>> {
        val params = BikeParams(
            inventoryStatuses = listOf(BikeInventoryStatus.IN_CITY.value),
            operativeStatuses = listOf(BikeOperativeStatus.STATIONED.value),
            boundingBox = BoundingBox(
                swCorner = Coordinates(
                    latitude = mapRect.startLat,
                    longitude = mapRect.startLong,
                ),
                neCorner = Coordinates(
                    latitude = mapRect.endLat,
                    longitude = mapRect.endLong,
                ),
            ),
        )
        return callAction { service.getBikes(params) }
    }

    override fun getBike(bikeId: String) : Flow<Result<Bike>> =
        callAction { service.getBike(bikeId) }

    override fun getParkings(mapRect: MapRect) : Flow<Result<List<Parking>>> {
        return callAction {
            service.getParkings("gt.${mapRect.startLat}", "lt.${mapRect.endLat}",
                               "gt.${mapRect.startLong}", "lt.${mapRect.endLong}")
        }
    }

    override fun getSlowZones(): Flow<Result<List<SlowZone>>> =
        callAction { service.getSlowZones() }

    override fun getMoveZones(): Flow<Result<List<MoveZone>>> =
        callAction { service.getMoveZones() }
}
