package ru.sitronics.velobike.data.repositories.rent

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.data.network.RentService
import ru.sitronics.velobike.data.repositories.BaseRepository
import ru.sitronics.velobike.domain.rent.ChooseParkingParams
import ru.sitronics.velobike.domain.rent.Feedback
import ru.sitronics.velobike.domain.rent.FeedbackRes
import ru.sitronics.velobike.domain.rent.FinishRentParams
import ru.sitronics.velobike.domain.rent.FinishedRentOld
import ru.sitronics.velobike.domain.rent.Rent
import ru.sitronics.velobike.domain.rent.RentData
import ru.sitronics.velobike.domain.rent.RentRepository
import ru.sitronics.velobike.domain.rent.RentStatus
import ru.sitronics.velobike.domain.rent.StartRentParams
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RentRepositoryImp @Inject constructor(
    private val service: RentService,
    appContextProvider: AppContextProvider,
    gson: Gson,
) : BaseRepository<RentData>(appContextProvider, gson), RentRepository {

    override fun getData() : RentData =
        super.getData() ?: RentData()

    override fun saveData(data: RentData) {
        super.saveData(data)
    }

    override suspend fun checkStatus(rentId: String, frameNumber: String) : Result<RentStatus> =
        callAction { service.checkStatus(rentId, frameNumber) }

    override suspend fun checkActiveRent() : Result<List<Rent>> =
        callAction { service.checkActiveRent() }

    override suspend fun checkActiveRentOld(uid: String) : Result<List<Rent>> =
        callAction { service.checkActiveRentOld("eq.$uid") }

    override suspend fun checkFinishedRentOld(customerId: String, rentId: String) : Result<List<FinishedRentOld>> =
        callAction { service.checkFinishedRentOld("eq.$customerId", "eq.$rentId") }

    override suspend fun startRent(params: StartRentParams) : Result<RentStatus> =
        callAction { service.startRent(params) }

    override suspend fun finishRent(params: FinishRentParams) : Result<RentStatus> =
        callAction { service.finishRent(params.id, params) }

    override suspend fun chooseParking(rentId: String, params: ChooseParkingParams) : Result<RentStatus> =
        callAction { service.chooseParking(rentId, params) }

    override suspend fun uploadPhotoRent(rentId: String, imagePath: String) : Result<Boolean> {
        val image = File(imagePath)
        val requestBody = image.asRequestBody("image/jpeg".toMediaType())
        val filePart = MultipartBody.Part.createFormData(
            "photo",
            image.name,
            requestBody
        )
        return callAction { service.uploadPhotoRent(rentId, filePart) }
    }

    override suspend fun finishRentAfterUploadPhoto(rentId: String) : Result<RentStatus> =
        callAction { service.finishRentAfterUploadPhoto(rentId) }

    override suspend fun sendFeedback(feedback: Feedback) : Result<FeedbackRes> =
        callAction { service.sendFeedback(feedback) }

    override suspend fun returnToActiveRent(rentId: String) : Result<RentStatus> =
        callAction { service.returnToActiveRent(rentId) }

    override suspend fun unlockWheel(rentId: String) : Result<Boolean> =
        callAction { service.unlockWheel(rentId) }
}
