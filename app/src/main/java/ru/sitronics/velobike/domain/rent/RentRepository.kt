package ru.sitronics.velobike.domain.rent

import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.Result

interface RentRepository {
    fun getData(): RentData
    fun saveData(data: RentData)
    fun startRent(bikeId: String, latitude: Double, longitude: Double) : Flow<Result<RentStatus>>
    fun checkStatus(rentId: Int, deviceId: String) : Flow<Result<RentStatus>>
}