package ru.sitronics.velobike.presentation.help

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.tools.BackPressHandler
import ru.sitronics.velobike.tools.Logg

enum class SupportScreen {
    BASE, CALL_US
}

@Composable
fun BoxScope.SupportScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var showScreen: SupportScreen by remember { mutableStateOf(SupportScreen.BASE) }

    Column (
        modifier = Modifier
            .matchParentSize()
            .background(Color.White)
            .padding(top = 16.dp)
    ) {
        Text(
            text = context.getString(R.string.support),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        )

        HelpItem(R.string.call_us, R.drawable.phone) {
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

private fun callToSupport(context: Context) {
    try {
        val intent = Intent(
            Intent.ACTION_DIAL,
            Uri.fromParts("tel", context.getString(R.string.support_phone), null)
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_NO_USER_ACTION
        context.startActivity(intent)
    } catch (e: Exception) {
        Logg.e(e)
    }
}

