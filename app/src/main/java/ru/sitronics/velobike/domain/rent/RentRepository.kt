package ru.sitronics.velobike.domain.rent

import ru.sitronics.velobike.data.Result

interface RentRepository {
    fun getData(): RentData
    fun saveData(data: RentData)
    suspend fun checkStatus(rentId: String, frameNumber: String) : Result<RentStatus>
    suspend fun checkActiveRent() : Result<List<Rent>>
    suspend fun checkActiveRentOld(uid: String) : Result<List<Rent>>
    suspend fun checkFinishedRentOld(customerId: String, rentId: String) : Result<List<FinishedRentOld>>
    suspend fun startRent(params: StartRentParams) : Result<RentStatus>
    suspend fun finishRent(params: FinishRentParams) : Result<RentStatus>
    suspend fun chooseParking(rentId: String, params: ChooseParkingParams) : Result<RentStatus>
    suspend fun uploadPhotoRent(rentId: String, imagePath: String) : Result<Boolean>
    suspend fun finishRentAfterUploadPhoto(rentId: String) : Result<RentStatus>
    suspend fun sendFeedback(feedback: Feedback) : Result<FeedbackRes>
    suspend fun returnToActiveRent(rentId: String) : Result<RentStatus>
    suspend fun unlockWheel(rentId: String) : Result<Boolean>
}