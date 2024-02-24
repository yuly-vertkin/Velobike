package ru.sitronics.velobike.domain.history

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.Result

interface HistoryRepository {
    fun getData(): HistoryData
    fun saveData(data: HistoryData)
    fun getHistory(params: HistoryParams) : Flow<Result<HistoryItemPackage>>
    fun getHistoryPagingSource(type: HistoryType?) : PagingSource<Int, HistoryItem>
}