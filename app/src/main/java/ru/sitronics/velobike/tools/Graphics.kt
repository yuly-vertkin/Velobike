package ru.sitronics.velobike.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.DisplayMetrics
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView
import ru.sitronics.velobike.presentation.map.DialogSize
import ru.sitronics.velobike.presentation.map.moveMap

private const val EMPTY_BITMAP_WIDTH = 28
private const val EMPTY_BITMAP_HEIGHT = 18

fun Context.getBitmapFromVectorDrawable(
    @DrawableRes drawableId: Int,
    emptyWidth: Int = EMPTY_BITMAP_WIDTH,
    emptyHeight: Int = EMPTY_BITMAP_HEIGHT,
): Bitmap {
    val scale = resources.displayMetrics.density.toInt()
    val drawable = try { ContextCompat.getDrawable(this, drawableId) } catch (_: Exception) { null }

    val bitmap = Bitmap.createBitmap(
        drawable?.intrinsicWidth ?: (emptyWidth * scale),
        drawable?.intrinsicHeight ?: (emptyHeight * scale),
        Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable?.setBounds(0, 0, canvas.width, canvas.height)
    drawable?.draw(canvas)

    return bitmap
}

fun Bitmap.drawText(context: Context, text: String, textSize: Int,
                    @ColorInt textColor: Int = Color.WHITE, textOnRight: Boolean = false) : Bitmap {
    val bitmap = copy(config ?: Bitmap.Config.ARGB_8888, true)

    val scale: Float = context.resources.displayMetrics.density
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = textColor
    paint.textSize = textSize * scale
    paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)

    val bounds = Rect()
    paint.getTextBounds(text, 0, text.length, bounds)
    val x = (bitmap.width - bounds.width()) / (if (textOnRight) 1.5f else 2f)
    val y = (bitmap.height + bounds.height()) / 2f
    canvas.drawText(text, x, y, paint)

    return bitmap
}

fun Bitmap.drawChatBitmap() : Bitmap {
    val bitmap = createBitmap(width * 2, height * 2, config)

    val canvas = Canvas(bitmap)
    val paint = Paint()
    var x = width / 2f
    var y = height / 2f

    canvas.drawBitmap(this, x, y, paint)
    paint.color = Color.RED

    x = width * 1.5f
    y = height / 2f
    val radius = width / 4f
    canvas.drawCircle(x, y, radius, paint)

    return bitmap
}

/*
fun Bitmap.drawChatBitmap() : Bitmap {
    val bitmap = copy(config ?: Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    paint.color = Color.RED

    val x = width * 0.75f
    val y = height * 0.25f
    val radius = width * 0.25f
    canvas.drawCircle(x, y, radius, paint)

    return bitmap
}
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetState.onSizeChanged(onSizeChanged: (Int) -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    LaunchedEffect(currentValue) {
        if (isVisible)  {
            val height = configuration.screenHeightDp - requireOffset().toInt().pxToDp(context) - PADDING_CORRECTION
            onSizeChanged(height)
        }
        else onSizeChanged(0)
    }
}

fun moveMapForObject(mapView: MapView, latitude: Double, longitude: Double, size: DialogSize) {
    if (latitude == 0.0 || longitude == 0.0 || size.width == 0 || size.height == 0 || size.mapHeight == 0) return

    moveMap(mapView, Point(latitude, longitude))
    try {
        mapView.mapWindow.focusRect = ScreenRect(
            ScreenPoint(0f, 0f),
            ScreenPoint(size.width.toFloat(), (size.mapHeight - size.height).toFloat()),
        )
    } catch (_: Exception) {}
}

fun Int.pxToDp(context: Context) : Int =
    this * DisplayMetrics.DENSITY_DEFAULT / context.resources.displayMetrics.densityDpi

fun Int.dpToPx(context: Context) : Int = (this * context.resources.displayMetrics.density).toInt()


const val PADDING_CORRECTION = 48
