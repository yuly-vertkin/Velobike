package ru.sitronics.velobike.presentation.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.history.HistoryItem
import ru.sitronics.velobike.tools.getDateTimeStr
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    contentPadding: PaddingValues,
    historyViewModel: HistoryViewModel = viewModel()
) {
    val historyTripItems = historyViewModel.historyTripItems.collectAsLazyPagingItems()
    val historyPayItems = historyViewModel.historyPayItems.collectAsLazyPagingItems()
    val tabData = listOf(stringResource(R.string.trip_tab), stringResource(R.string.pay_tab))
    val pagerState = rememberPagerState(pageCount = { tabData.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        Text(
            text = stringResource(R.string.history_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        )

        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabData.forEachIndexed { index, str ->
                Tab(
                    selected = pagerState.currentPage == index,
                    text = { Text(text = str) },
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) }},
                )
            }
        }

        HorizontalPager(state = pagerState) { page ->
            val historyItems = when (page) {
                HISTORY_TRIP_TYPE -> historyTripItems
                HISTORY_PAY_TYPE -> historyPayItems
                else -> null
            }
            historyItems?.let {
                HistoryPagingScreen(it, page)
            }
        }
    }
}

@Composable
fun HistoryPagingScreen(historyItems: LazyPagingItems<HistoryItem>, page: Int) {
    if (historyItems.loadState.refresh != LoadState.Loading) {
        LazyColumn {
            items(count = historyItems.itemCount) { index ->
                historyItems[index]?.let { item ->
                    when (page) {
                        HISTORY_TRIP_TYPE -> RideItemContent(item, index)
                        HISTORY_PAY_TYPE -> PayItemContent(item, index)
                    }
                }
            }

            if (historyItems.loadState.append == LoadState.Loading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun RideItemContent(item: HistoryItem, index: Int) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 12.dp)
    ) {
        Text(
            text = getDateTimeStr(item.startDate, "dd MMMM yyyy, EEEE"),
            fontWeight = FontWeight.Bold,
            color = Color.LightGray,
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            val endDate = Date(item.startDate.time + (item.duration?.let { it.time } ?: 0 ))
            Text(
                text = stringResource(R.string.str_str,
                    getDateTimeStr(item.startDate, "HH:mm"),
                    getDateTimeStr(endDate, "HH:mm")
                ),
                color = Color.Gray,
            )

            Text(
                text = "${item.price?.toInt()} ₽",
                fontWeight = FontWeight.Bold,
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp), 1.dp, Color.LightGray)

        Text(
            text = "№ " + item.startBikeParkingNumber.orEmpty(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = item.startBikeParkingAddress.orEmpty(),
            fontSize = 14.sp,
            color = Color.Gray,
        )

        Text(
            text = "№ " + item.endBikeParkingNumber.orEmpty(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp)
        )

        Text(
            text = item.endBikeParkingAddress.orEmpty(),
            fontSize = 14.sp,
            color = Color.Gray,
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${item.coveredDistance.toInt()} м.",
                fontSize = 14.sp,
                color = Color.Gray,
            )

            Text(
                text = item.duration?.let { getDateTimeStr(it, "HH:mm") } ?: "",
                fontSize = 14.sp,
                color = Color.Gray,
            )

            Text(
                text = item.bikeId.orEmpty(),
                fontSize = 14.sp,
                color = Color.Gray,
            )
        }
    }
}

@Composable
fun PayItemContent(item: HistoryItem, index: Int) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = item.contract.orEmpty(),
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "- ${item.price?.toInt()} ₽",
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.pay_tariff),
                fontSize = 12.sp,
            )

            Text(
                text = getDateTimeStr(item.startDate, "dd MMMM yyyy, HH:mm"),
                fontSize = 12.sp,
            )
        }
    }
}

private const val HISTORY_TRIP_TYPE = 0
private const val HISTORY_PAY_TYPE = 1

