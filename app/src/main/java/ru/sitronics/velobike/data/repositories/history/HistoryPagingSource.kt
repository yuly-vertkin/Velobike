package ru.sitronics.velobike.data.repositories.history

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import ru.sitronics.velobike.data.network.HistoryService
import ru.sitronics.velobike.domain.history.HISTORY_PAGINATION_SIZE
import ru.sitronics.velobike.domain.history.HistoryItem
import ru.sitronics.velobike.domain.history.HistoryType
import java.io.IOException

class HistoryPagingSource(
    val service: HistoryService,
    private val type: HistoryType? = null
) : PagingSource<Int, HistoryItem>() {
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, HistoryItem> {
        try {
            // Start refresh at page 1 if undefined.
            val currentPage = params.key ?: 1
            val offset = (currentPage - 1) * HISTORY_PAGINATION_SIZE

            val response = service.getHistory(limit = HISTORY_PAGINATION_SIZE, offset = /*historyParams.*/offset, type = type?.value)

            return LoadResult.Page(
                data = response.items,
//                prevKey = null, // Only paging forward.
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (response.hasMore) currentPage + 1 else null
            )
        } catch (e: IOException) {
            return LoadResult.Error(e)
        } catch (e: HttpException) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, HistoryItem>): Int? {
        // Try to find the page key of the closest page to anchorPosition from
        // either the prevKey or the nextKey; you need to handle nullability here.
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey are null -> anchorPage is the initial page, so return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}