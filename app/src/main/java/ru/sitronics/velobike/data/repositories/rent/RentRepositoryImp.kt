package ru.sitronics.velobike.data.repositories.rent

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.data.network.RentService
import ru.sitronics.velobike.data.repositories.BaseRepository
import ru.sitronics.velobike.domain.rent.RentData
import ru.sitronics.velobike.domain.rent.RentRepository
import ru.sitronics.velobike.domain.rent.RentStatus
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

    override fun startRent(bikeId: String, latitude: Double, longitude: Double) : Flow<Result<RentStatus>> {
        val params = StartRentParams(
            bikeSerialNumber = bikeId,
            isUsedQr = true,
            clientGeoPosition = ClientGeoPosition(
                lat = latitude,
                lon = longitude,
            ),
        )
        return callAction { service.startRent(params) }
    }

    override fun checkStatus(rentId: Int, deviceId: String) : Flow<Result<RentStatus>> {
        return callAction { service.checkStatus(rentId, deviceId) }
    }
}
