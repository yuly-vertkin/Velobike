package ru.sitronics.velobike.presentation.help

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.BuildConfig
import ru.sitronics.velobike.R
import ru.sitronics.velobike.tools.BackPressHandler
import ru.sitronics.velobike.ui.theme.VelobikeTheme

@Composable
fun HelpScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    var showHowToUse by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .background(Color.White)
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = context.getString(R.string.help),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { showHowToUse = true },
            ) {
                Icon(painterResource(R.drawable.how_to_use), "")

                Text(
                    text = context.getString(R.string.how_to_use),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
        Text(
            text = context.getString(R.string.version, BuildConfig.VERSION_NAME),
            color = Color.LightGray,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .align(Alignment.BottomCenter)
        )

        if (showHowToUse) {
            HowToUseScreen { showHowToUse = false }
        }
    }
}

@Composable
fun BoxScope.HowToUseScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var showFirstTrip by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .matchParentSize()
            .background(Color.White)
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = context.getString(R.string.how_to_use),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )
            HowToUseItem(R.string.first_trip, R.string.first_trip_desc, R.drawable.first_trip) {
                showFirstTrip = true
            }
            HowToUseItem(R.string.bike_safety, R.string.bike_safety_desc, R.drawable.bike_safety) {
                showFirstTrip = true
            }
        }
    }

    if (showFirstTrip) {
        FirstTripScreen { showFirstTrip = false }
    }

    BackPressHandler(onBackPressed = onBack)
}

@Composable
private fun HowToUseItem(
    @StringRes textId: Int, @StringRes addTextId: Int, @DrawableRes imageId: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
    ) {
        Column (
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = context.getString(textId),
                lineHeight = 16.sp,
            )
            Text(
                text = context.getString(addTextId),
                fontSize = 14.sp,
                lineHeight = 14.sp,
                color = Color.LightGray,
            )
        }
        Image(
            painter = painterResource(imageId),
            contentDescription = "",
            modifier = Modifier
                .size(100.dp)
                .padding(start = 16.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FirstTripScreen(
    onBack: () -> Unit,
) {
    val slides = remember { listOf(
        FirstTripSlide(R.string.first_trip_slide1, R.string.first_trip_slide1_desc, R.drawable.first_trip_slide1),
        FirstTripSlide(R.string.first_trip_slide2, R.string.first_trip_slide2_desc, R.drawable.first_trip_slide2),
        FirstTripSlide(R.string.first_trip_slide3_4_5_6, R.string.first_trip_slide3_desc, R.drawable.first_trip_slide3),
        FirstTripSlide(R.string.first_trip_slide3_4_5_6, R.string.first_trip_slide4_desc, R.drawable.first_trip_slide4),
        FirstTripSlide(R.string.first_trip_slide3_4_5_6, R.string.first_trip_slide5_desc, R.drawable.first_trip_slide5),
        FirstTripSlide(R.string.first_trip_slide3_4_5_6, R.string.first_trip_slide6_desc, R.drawable.first_trip_slide6),
    )}
    val pagerState = rememberPagerState(pageCount = { FIRST_TRIP_SLIDE_NUM })
    val context = LocalContext.current

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .background(Color.White)
    ) {
        PageIndicator(pagerState)

        HorizontalPager(state = pagerState) { page ->
            Column {
                Text(
                    text = context.getString(slides[page].textId),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                )
                Text(
                    text = context.getString(slides[page].textDescId),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                )
                Image(
                    painter = painterResource(id = slides[page].imageId),
                    contentDescription = "",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    BackPressHandler(onBackPressed = onBack)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColumnScope.PageIndicator(pagerState: PagerState) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .align(Alignment.CenterHorizontally)
            .padding(vertical = 16.dp),
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(16.dp)
            )
        }
    }
}

private const val FIRST_TRIP_SLIDE_NUM = 6

class FirstTripSlide(@StringRes val textId: Int, @StringRes val textDescId: Int, @DrawableRes val imageId: Int)

@Preview(showBackground = true, widthDp = 500, heightDp = 500)
@Composable
fun HowToUseScreenPreview() {
    VelobikeTheme {
        Box {
            HowToUseScreen { }
        }
    }
}


