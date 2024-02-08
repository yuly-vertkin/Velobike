package ru.sitronics.velobike.presentation.profile

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ru.sitronics.velobike.domain.profile.Tariff

@Composable
fun TariffScreen(
    tariffs: List<Tariff>,
    onAction: (ProfileIntent) -> Unit,
) {
    val context = LocalContext.current

    LazyColumn {
        items(tariffs) { tariff ->
            Text(tariff.name)
        }
    }
}