package ru.sitronics.velobike.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState.Search
import ru.sitronics.velobike.tools.BackPressHandler
import ru.sitronics.velobike.tools.getNumberStr

@Composable
fun SearchDialog(uiState: MapUiState, onClick: (String) -> Unit, onChange: (String) -> Unit) {
    var state by remember { mutableStateOf(CLOSE) }
    var searchStr by remember { mutableStateOf("") }
    var parkings by remember { mutableStateOf<List<Parking>>(emptyList()) }
    val context = LocalContext.current

    if (uiState is Search && state != CLOSING) {
        parkings = uiState.parkings.orEmpty()
        state = true.toDialogState()
    } else if (uiState !is Search && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(all = 12.dp)
        ) {
            OutlinedTextField(
                value = searchStr,
                onValueChange = { searchStr = it; onChange(it) },
                label = { Text(context.getString(R.string.search_label)) },
                leadingIcon = { Icon(painterResource(R.drawable.search_gray), null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )

            LazyColumn {
                items(parkings) {
                    Column(
                        modifier = Modifier.clickable { state = CLOSING; searchStr = ""; onClick(it.id) }
                    ) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), 1.dp, Color.LightGray)
                        Text(text = "№ ${it.id} ${it.address}    ${getDistanceStr(it.distance)}")
                    }
                }
            }
        }

        BackPressHandler(onBackPressed = { state = CLOSING; searchStr = "" })
    }
}

private fun getDistanceStr(distance: Float?) : String {
    return distance?.let {
        val dst = if (it >= 1000) it / 1000 else it
        val suffix = if (it >= 1000) " км." else " м."
        getNumberStr(dst, "##0.0") + suffix
    } ?: ""
}
