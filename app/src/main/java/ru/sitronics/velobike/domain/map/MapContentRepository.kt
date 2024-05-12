package ru.sitronics.velobike.domain.map

import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.domain.MapRect

interface MapContentRepository {
    fun getData(): MapContentData
    fun saveData(data: MapContentData)
    suspend fun getBikes(mapRect: MapRect) : Result<List<Bike>>
    suspend fun getBike(bikeId: String) : Result<Bike>
    suspend fun getParkings(mapRect: MapRect) : Result<List<Parking>>
    suspend fun getSlowZones(): Result<List<SlowZone>>
    suspend fun getMoveZones(): Result<List<MoveZone>>
}