package ru.sitronics.velobike.presentation.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HistoryScreen(
    contentPadding: PaddingValues,
) {
    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Text(
            text = "HistoryScreen is under development",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
