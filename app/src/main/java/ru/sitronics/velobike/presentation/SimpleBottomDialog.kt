package ru.sitronics.velobike.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.sitronics.velobike.tools.toDp

@Composable
fun SimpleBottomDialog(
    onDismissRequest: () -> Unit,
    onSizeChanged: (Int) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismissRequest(); onSizeChanged(0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(Color.White, RoundedCornerShape(16.dp, 16.dp))
                // used instead .clickable { } to avoid click visual effects
                .pointerInput(null) { detectTapGestures { } }
                .onGloballyPositioned { coordinates ->
                    val height = coordinates.size.height.toDp(context)
                    onSizeChanged(height)
                }

        ) {
            content()
        }
    }
}