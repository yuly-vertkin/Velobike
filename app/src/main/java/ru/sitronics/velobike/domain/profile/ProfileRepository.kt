package ru.sitronics.velobike.domain.profile

import ru.sitronics.velobike.data.Result

interface ProfileRepository {
    fun getData(): ProfileData
    fun saveData(data: ProfileData)
    suspend fun getProfile() : Result<Profile>
    suspend fun getTariffs() : Result<List<Tariff>>
    suspend fun getTariff(id: String) : Result<Tariff>
    suspend fun getCards() : Result<List<Card>>
    suspend fun payTariff(params: TariffPaymentParams): Result<TariffPayment>
    suspend fun getLKPStatus(userId: String) : Result<LKPStatusData>
    suspend fun authInMetro(userId: String, phone: String) : Result<MetroPasswordParameters>
    suspend fun createAuthToken(userId: String, code: String) : Result<Boolean>
}