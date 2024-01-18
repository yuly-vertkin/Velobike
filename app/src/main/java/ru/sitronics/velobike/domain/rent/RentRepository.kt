package ru.sitronics.velobike.domain.rent

import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.Result

interface RentRepository {
    fun getData(): RentData
    fun saveData(data: RentData)
    fun startRent(params: StartRentParams) : Flow<Result<RentStatus>>
    fun finishRent(params: FinishRentParams) : Flow<Result<RentStatus>>
    fun checkStatus(rentId: Int, deviceId: String) : Flow<Result<RentStatus>>
    fun checkActiveRent() : Flow<Result<List<ActiveRent>>>
}