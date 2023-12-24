package ru.sitronics.velobike.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.yandex.runtime.image.ImageProvider
import java.util.UUID

class ClusterImageProvider (
    private val context: Context,
    private val size: Int,
    @DrawableRes private val drawableId: Int,
    @ColorInt private val textColor: Int = Color.BLACK,
    ) : ImageProvider() {

    override fun getId(): String =
        "ClusterImageProvider:" + UUID.randomUUID().toString()

    override fun getImage(): Bitmap {
        val scale = context.resources.displayMetrics.density.toInt()
        return (context.getBitmapFromVectorDrawable(drawableId) ?:
                Bitmap.createBitmap(EMPTY_BITMAP_WIDTH * scale, EMPTY_BITMAP_HEIGHT * scale, Bitmap.Config.ARGB_8888))
                .drawText(context, getText(size), TEXT_SIZE, textColor)
    }

    private fun getText(size: Int) : String {
        return when {
            size < 0 -> "0"
            size < 10 -> size.toString()
            size in 10..49 -> "10+"
            size in 50..99 -> "50+"
            size in 100..199 -> "100+"
            size in 200..999 -> "200+"
            else -> "1000+"
        }
    }

    companion object {
        private const val TEXT_SIZE = 14
        private const val EMPTY_BITMAP_WIDTH = 28
        private const val EMPTY_BITMAP_HEIGHT = 18
    }
}