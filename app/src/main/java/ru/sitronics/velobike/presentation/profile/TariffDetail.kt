package ru.sitronics.velobike.presentation.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import coil.compose.AsyncImage
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.profile.Tariff
import ru.sitronics.velobike.tools.BackPressHandler

@Composable
fun TariffDetail(
    tariff: Tariff,
    canBuy: Boolean,
    onAction: (Tariff?) -> Unit,
) {
    val context = LocalContext.current
    var agree by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = tariff.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        AsyncImage(
            model = tariff.icon,
            error = painterResource(R.drawable.tariff_default),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )

        Text(
            text = context.getString(R.string.access_cost),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "${tariff.cost?.toInt() ?: 0} ₽",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
        )

        if (canBuy) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 64.dp)
                    .padding(top = 200.dp),
            ) {
                Checkbox(
                    checked = agree,
                    onCheckedChange = { agree = it }
                )
                Text(
                    text = context.getString(R.string.agree),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Button(
                onClick = { onAction(tariff) },
                enabled = agree,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = context.getString(R.string.buy_tariff))
            }
        }
    }

    BackPressHandler(onBackPressed = { onAction(null) })
}
