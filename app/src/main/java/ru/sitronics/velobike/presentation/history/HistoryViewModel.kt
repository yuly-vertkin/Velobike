package ru.sitronics.velobike.presentation.history

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.history.HISTORY_PAGINATION_SIZE
import ru.sitronics.velobike.domain.history.HistoryItem
import ru.sitronics.velobike.domain.history.HistoryRepository
import ru.sitronics.velobike.domain.history.HistoryType
import ru.sitronics.velobike.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    val historyTripItems = getPagingItem(HistoryType.RIDE)
    val historyPayItems = getPagingItem(HistoryType.PAY)

//    init {
//        getHistory()
//    }

    private fun getPagingItem(type: HistoryType) : Flow<PagingData<HistoryItem>> =
        Pager(
            config = PagingConfig(pageSize = HISTORY_PAGINATION_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { historyRepository.getHistoryPagingSource(type) }
        ).flow.cachedIn(viewModelScope)

/*
    private fun getHistory() {
        val params = HistoryParams(
            limit = HISTORY_PAGINATION_SIZE,
            offset = 0,
            type = HistoryType.RIDE,
        )
        processNetworkCall(
            action = { historyRepository.getHistory(params) },
            onSuccess = {
                Logg.d("!!!! getHistory success")
//                changeState(HistoryUiState.Normal(it))
            },
            onError = {
                Logg.d("!!!! getHistory error")
            },
            callName = "getHistory"
        )
    }
 */
}
