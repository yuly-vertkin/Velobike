package ru.sitronics.velobike.presentation.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.profile.Tariff
import ru.sitronics.velobike.domain.profile.TariffBikeType
import ru.sitronics.velobike.tools.BackPressHandler
import ru.sitronics.velobike.ui.theme.HeaderBackgroundColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TariffsScreen(
    tariffs: List<Tariff>,
    onAction: (Tariff?) -> Unit,
) {
    val tabData = listOf(stringResource(R.string.old_bike_tab), stringResource(R.string.new_bike_tab))
    val pagerState = rememberPagerState(pageCount = { tabData.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.choose_tariff),
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

        HorizontalPager(state = pagerState) { i ->
            val curTariffs = tariffs.filter {
                it.tariffBikeType == TariffBikeType.values()[i]
            }
            TariffContent(curTariffs) {
                onAction(it)
            }
        }
    }

    BackPressHandler(onBackPressed = { onAction(null) })
}

@Composable
fun TariffContent(tariffs: List<Tariff>, onClick: (Tariff) -> Unit) {
    LazyColumn {
        items(tariffs) { tariff ->
            Row(
                modifier = Modifier
                    .clickable { onClick(tariff) }
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                AsyncImage(
                    model = tariff.icon,
                    error = painterResource(R.drawable.tariff_default),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )

                Column (modifier = Modifier.padding(start = 16.dp)) {
                    if (tariff.name.isNotEmpty())
                        Text(
                            text = tariff.name,
                            fontWeight = FontWeight.Bold,
                        )
                    if (!tariff.description.isNullOrEmpty())
                        Text(
                            text = tariff.description,
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                        )
                    if (!tariff.descriptionForList.isNullOrEmpty())
                        Text(
                            text = tariff.descriptionForList,
                            fontSize = 12.sp,
                            color = HeaderBackgroundColor,
                            lineHeight = 12.sp,
                        )
                }
            }
        }
    }
}