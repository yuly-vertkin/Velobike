package ru.sitronics.velobike.domain.content

import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.domain.MapRect

interface MapContentRepository {
    fun getData(): MapContentData
    fun saveData(data: MapContentData)
    fun getBikes(mapRect: MapRect) : Flow<Result<List<Bike>>>
    fun getBike(bikeId: String) : Flow<Result<Bike>>
    fun getParkings(mapRect: MapRect) : Flow<Result<List<Parking>>>
}