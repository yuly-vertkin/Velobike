package ru.sitronics.velobike.data.repositories.history

import android.content.Context
import com.google.gson.Gson
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.data.network.HistoryService
import ru.sitronics.velobike.data.repositories.BaseRepository
import ru.sitronics.velobike.domain.history.HistoryData
import ru.sitronics.velobike.domain.history.HistoryItemPackage
import ru.sitronics.velobike.domain.history.HistoryParams
import ru.sitronics.velobike.domain.history.HistoryRepository
import ru.sitronics.velobike.domain.history.HistoryType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImp @Inject constructor(
    private val service: HistoryService,
    appContext: Context,
    gson: Gson,
) : BaseRepository<HistoryData>(appContext, gson), HistoryRepository {

    override fun getData() : HistoryData =
        super.getData() ?: HistoryData()

    override fun saveData(data: HistoryData) {
        super.saveData(data)
    }

    override suspend fun getHistory(params: HistoryParams): Result<HistoryItemPackage> =
        callAction { service.getHistory(limit = params.limit, offset = params.offset, type = params.type?.value) }

    override fun getHistoryPagingSource(type: HistoryType?) =
        HistoryPagingSource(service, type)
}
