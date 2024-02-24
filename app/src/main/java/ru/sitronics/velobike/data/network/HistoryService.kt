package ru.sitronics.velobike.data.network

import retrofit2.http.GET
import retrofit2.http.Query
import ru.sitronics.velobike.domain.history.HistoryItemPackage

interface HistoryService {
    @GET(GET_HISTORY)
    suspend fun getHistory(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("show_tracking") showTracking: Int = 1,
        @Query("show_assessment") showAssessment: Int = 1,
        @Query("type") type: String? = null
    ): HistoryItemPackage
}