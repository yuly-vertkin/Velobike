package ru.sitronics.velobike.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

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
