package ru.sitronics.velobike.presentation.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.tools.BackPressHandler
import ru.sitronics.velobike.tools.MenuItem
import ru.sitronics.velobike.tools.callToSupport

enum class SupportScreen {
    BASE, CALL_US
}

@Composable
fun SupportScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var showScreen: SupportScreen by remember { mutableStateOf(SupportScreen.BASE) }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.support),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        )

        MenuItem(R.string.call_us, R.drawable.phone) {
            /*showScreen = SupportScreen.CALL_US*/
            callToSupport(context)
        }
    }

    when (showScreen) {
        SupportScreen.CALL_US -> {}
        else -> {}
    }

    BackPressHandler(onBackPressed = onBack)
}
