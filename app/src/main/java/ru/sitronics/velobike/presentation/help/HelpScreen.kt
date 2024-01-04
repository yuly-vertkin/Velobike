package ru.sitronics.velobike.presentation.help

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ru.sitronics.velobike.R

@Composable
fun HelpScreen(
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Text(
            text = context.getString(R.string.screen_not_ready),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
