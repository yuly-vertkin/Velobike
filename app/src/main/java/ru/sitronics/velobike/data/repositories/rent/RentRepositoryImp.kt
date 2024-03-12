package ru.sitronics.velobike.data.repositories.rent

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.data.network.RentService
import ru.sitronics.velobike.data.repositories.BaseRepository
import ru.sitronics.velobike.domain.rent.ActiveRent
import ru.sitronics.velobike.domain.rent.ChooseParkingParams
import ru.sitronics.velobike.domain.rent.FinishRentParams
import ru.sitronics.velobike.domain.rent.FinishedRentOld
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

    override fun checkStatus(rentId: Int, frameNumber: String) : Flow<Result<RentStatus>> =
        callAction { service.checkStatus(rentId, frameNumber) }

    override fun checkActiveRent() : Flow<Result<List<ActiveRent>>> =
        callAction { service.checkActiveRent() }

    override fun checkActiveRentOld(uid: String) : Flow<Result<List<ActiveRent>>> =
        callAction { service.checkActiveRentOld("eq.$uid") }

    override fun checkFinishedRentOld(customerId: String, rentId: Int) : Flow<Result<List<FinishedRentOld>>> =
        callAction { service.checkFinishedRentOld("eq.$customerId", "eq.$rentId") }

    override fun startRent(params: StartRentParams) : Flow<Result<RentStatus>> =
        callAction { service.startRent(params) }

    override fun finishRent(params: FinishRentParams) : Flow<Result<RentStatus>> =
        callAction { service.finishRent(params.id, params) }

    override fun chooseParking(rentId: Int, params: ChooseParkingParams) : Flow<Result<RentStatus>> =
        callAction { service.chooseParking(rentId, params) }

    override fun uploadPhotoRent(rentId: Int, imagePath: String) : Flow<Result<Boolean>> {
        val image = File(imagePath)
        val requestBody = image.asRequestBody("image/jpeg".toMediaType())
        val filePart = MultipartBody.Part.createFormData(
            "photo",
            image.name,
            requestBody
        )
        return callAction { service.uploadPhotoRent(rentId, filePart) }
    }

    override fun finishRentAfterUploadPhoto(rentId: Int) : Flow<Result<RentStatus>> =
        callAction { service.finishRentAfterUploadPhoto(rentId) }
}
