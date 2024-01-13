package ru.sitronics.velobike.presentation.help

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.BuildConfig
import ru.sitronics.velobike.R

enum class HelpScreen {
    BASE, HOW_TO_USE, SUPPORT
}

@Composable
fun HelpScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    var showScreen: HelpScreen by remember { mutableStateOf(HelpScreen.BASE) }

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

            HelpItem(R.string.how_to_use, R.drawable.how_to_use) { showScreen = HelpScreen.HOW_TO_USE }
            HelpItem(R.string.support, R.drawable.support) { showScreen = HelpScreen.SUPPORT }

        }
        Text(
            text = context.getString(R.string.version, BuildConfig.VERSION_NAME),
            color = Color.LightGray,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .align(Alignment.BottomCenter)
        )

        when (showScreen) {
            HelpScreen.HOW_TO_USE -> HowToUseScreen { showScreen = HelpScreen.BASE }
            HelpScreen.SUPPORT -> SupportScreen { showScreen = HelpScreen.BASE }
            else -> {}
        }
    }
}

@Composable
fun HelpItem(
    @StringRes textId: Int, @DrawableRes imageId: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Icon(painterResource(imageId), "")

        Text(
            text = context.getString(textId),
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

