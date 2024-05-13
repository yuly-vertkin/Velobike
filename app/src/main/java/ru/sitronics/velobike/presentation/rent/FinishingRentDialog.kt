package ru.sitronics.velobike.presentation.rent

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.SimpleBottomDialog
import ru.sitronics.velobike.presentation.map.DialogAction
import ru.sitronics.velobike.presentation.map.DialogState.CLOSE
import ru.sitronics.velobike.presentation.map.DialogState.CLOSING
import ru.sitronics.velobike.presentation.map.DialogState.SHOW
import ru.sitronics.velobike.presentation.map.MapUiState
import ru.sitronics.velobike.presentation.map.MapUiState.FinishingRent
import ru.sitronics.velobike.presentation.map.toDialogState
import ru.sitronics.velobike.tools.getImageLoader

@Composable
fun FinishingRentDialog(uiState: MapUiState, onSizeChanged: (Int) -> Unit, onAction: (DialogAction) -> Unit) {
    var state by remember { mutableStateOf(CLOSE) }

    if (uiState is FinishingRent && state != CLOSING) {
        state = uiState.show.toDialogState()
    } else if (uiState !is FinishingRent && state == CLOSING)
        state = CLOSE

    if (state == SHOW) {
        SimpleBottomDialog(
            onSizeChanged = { onSizeChanged(it.height) },
            onDismissRequest = { state = CLOSING; onAction(DialogAction.DISMISS) },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
            ) {
                IconButton(onClick = { state = CLOSING; onAction(DialogAction.BACK) }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = null,
                        tint = Color.LightGray,
                    )
                }

                Text(
                    text = stringResource(R.string.finish_rent),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = (-24).dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                PromptCard(R.string.close_wheel_lock, R.drawable.close_wheel_lock)
                PromptCard(R.string.close_chain, R.drawable.parking_electro)
            }

            Button(
                onClick = {
                    state = CLOSING; onSizeChanged(0)
                    onAction(DialogAction.CLICK)
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(all = 16.dp)
            ) {
                Text(stringResource(R.string.finish_rent_btn))
            }
        }
    }
}

@Composable
fun PromptCard(@StringRes textId: Int, @DrawableRes imageId: Int, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var isClicked by remember { mutableStateOf(false) }

    OutlinedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.Black),
        modifier = modifier.requiredSize(width = CARD_WIDTH.dp, height = CARD_HEIGHT.dp)
    ) {
        if (!isClicked)
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(textId),
                    lineHeight = 12.sp,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp),
                )

                Icon(
                    painter = painterResource(id = R.drawable.info),
                    contentDescription = null,
                    modifier = Modifier
                        .clickable {
                            isClicked = true
                            scope.launch { delay(SHOW_ANIMATION_TIME); isClicked = false }
                        }
                        .padding(all = 12.dp)
                        .size(ICON_SIZE.dp)
                        .scale(1.2f)
                        .align(Alignment.BottomEnd)
                )
            }
        else
            Image(
                painter = rememberAsyncImagePainter(imageId, getImageLoader()),
                contentDescription = null,
                modifier = Modifier
                    .width(CARD_WIDTH.dp)
                    .height(CARD_HEIGHT.dp)
            )
    }
}

private const val CARD_WIDTH = 170
private const val CARD_HEIGHT = 130
private const val ICON_SIZE = 40
private const val SHOW_ANIMATION_TIME = 4000L // ms
