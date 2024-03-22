package ru.sitronics.velobike.presentation.rent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.rent.ActiveRent
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.FinishedRent
import ru.sitronics.velobike.presentation.map.toDialogState
import ru.sitronics.velobike.tools.getTimeStr
import ru.sitronics.velobike.tools.onSizeChanged
import ru.sitronics.velobike.ui.theme.LightGrayBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishedRentDialog(uiState: MapUiState, onSizeChanged: (Int) -> Unit, onDismiss: () -> Unit, onClick: (ActiveRent?, Int) -> Unit) {
    var activeRent by remember { mutableStateOf<ActiveRent?>(null) }
    var state by remember { mutableStateOf(CLOSE) }
    var rating by remember { mutableStateOf(1f) } //default rating will be 1
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    sheetState.onSizeChanged(onSizeChanged)

    if (uiState is FinishedRent && state != CLOSING) {
        activeRent = uiState.activeRent
        state = true.toDialogState()
    } else if (uiState !is FinishedRent && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        activeRent?.let {
            ModalBottomSheet(
                onDismissRequest = { state = CLOSING; onDismiss() },
                sheetState = sheetState
            ) {
                Text(
                    text = context.getString(R.string.finished_rent),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 32.dp)
                        .offset(y = (-12).dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "${it.cost} ₽",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f)
                    )

                    Text(
                        text = getTimeStr(it.startTime),
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f)
                    )

                    Text(
                        text = "№" + it.frameNumber,
                        modifier = Modifier
                            .padding(start = 16.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightGrayBackgroundColor)
                        .padding(all = 16.dp)
                ) {
                    Text(
                        context.getString(R.string.rate_title),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    StarRatingBar(
                        maxStars = 5,
                        rating = rating,
                        onRatingChanged = { rating = it },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 16.dp)
                    )

                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    state = CLOSING
                                    onClick(activeRent, rating.toInt())
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(context.getString(R.string.rate_btn))
                    }
                }
            }
        }
    }
}

@Composable
fun StarRatingBar(
    maxStars: Int = 5,
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier,
) {
    val density = LocalDensity.current.density
    val starSize = (12f * density).dp
    val starSpacing = (0.5f * density).dp

    Row(
        modifier = modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Filled.Star else Icons.Default.Star
            val iconTintColor = if (isSelected) Color(0xFFFFC700) else Color(0x80CCCCCC)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            onRatingChanged(i.toFloat())
                        }
                    )
                    .width(starSize)
                    .height(starSize)
            )

            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}
