package ru.sitronics.velobike.domain.profile

import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.Result

interface ProfileRepository {
    fun getData(): ProfileData
    fun saveData(data: ProfileData)
    fun getProfile() : Flow<Result<Profile>>
    fun getTariffs() : Flow<Result<List<Tariff>>>
    fun getTariff(id: String) : Flow<Result<Tariff>>
    fun getCards() : Flow<Result<List<Card>>>
    fun payTariff(params: TariffPaymentParams): Flow<Result<TariffPayment>>
}