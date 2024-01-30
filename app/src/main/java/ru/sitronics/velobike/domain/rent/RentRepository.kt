package ru.sitronics.velobike.domain.rent

import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.Result

interface RentRepository {
    fun getData(): RentData
    fun saveData(data: RentData)
    fun checkStatus(rentId: Int, deviceId: String) : Flow<Result<RentStatus>>
    fun checkActiveRent() : Flow<Result<List<ActiveRent>>>
    fun checkActiveRentOld(uid: String) : Flow<Result<List<ActiveRent>>>
    fun startRent(params: StartRentParams) : Flow<Result<RentStatus>>
    fun finishRent(params: FinishRentParams) : Flow<Result<RentStatus>>
    fun uploadPhotoRent(rentId: Int, deviceId: String, imagePath: String) : Flow<Result<Boolean>>
    fun finishRentAfterUploadPhoto(rentId: Int) : Flow<Result<RentStatus>>
}