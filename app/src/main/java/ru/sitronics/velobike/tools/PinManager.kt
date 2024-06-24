package ru.sitronics.velobike.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.map.BIKE_BATTERY_POWER_HIGH
import ru.sitronics.velobike.presentation.map.BIKE_BATTERY_POWER_LOW
import kotlin.math.max
import kotlin.math.min

enum class PinType {
    BIKE, BIKE_ZOOM_SMALL, BIKE_ZOOM,
    STATION, STATION_PARK, STATION_ZOOM, STATION_PARK_ZOOM,
    PARKING, BIKE_IMAGE, BIKE_IMAGE_EL, BIKE_IMAGE_M
}

private enum class PinKey {
    PIN_1, PIN_2, PIN_1_1, PIN_2_1, PIN_2_2,
    PIN_ZOOM_1, PIN_ZOOM_2, PIN_ZOOM_3, PIN_ZOOM_4,
    PIN_PARK_1, PIN_PARK_2, PIN_PARK_1_1, PIN_PARK_2_1, PIN_PARK_2_2,
    PIN_PARK_ZOOM_1, PIN_PARK_ZOOM_2, PIN_PARK_ZOOM_3, PIN_PARK_ZOOM_4,
    BIKE_LOW, BIKE_MIDDLE, BIKE_HIGH, PARKING,
    BIKE_IMAGE, BIKE_IMAGE_EL, BIKE_IMAGE_M, PARK_IMAGE_EL, PARK_IMAGE_M
}

class PinManager (private val context: Context) {
    private val pinBitmaps = hashMapOf<PinKey, Bitmap>()
    private val numberSizes = listOf(21, 29, 35, 42)

    init {
        pinBitmaps[PinKey.PIN_1] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_1)
        pinBitmaps[PinKey.PIN_2] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_2)
        pinBitmaps[PinKey.PIN_1_1] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_1_1)
        pinBitmaps[PinKey.PIN_2_1] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_2_1)
        pinBitmaps[PinKey.PIN_2_2] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_2_2)

        pinBitmaps[PinKey.PIN_ZOOM_1] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_zoom_1)
        pinBitmaps[PinKey.PIN_ZOOM_2] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_zoom_2)
        pinBitmaps[PinKey.PIN_ZOOM_3] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_zoom_3)
        pinBitmaps[PinKey.PIN_ZOOM_4] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_zoom_4)

        pinBitmaps[PinKey.PIN_PARK_1] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_park_1)
        pinBitmaps[PinKey.PIN_PARK_2] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_park_2)
        pinBitmaps[PinKey.PIN_PARK_1_1] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_park_1_1)
        pinBitmaps[PinKey.PIN_PARK_2_1] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_park_2_1)
        pinBitmaps[PinKey.PIN_PARK_2_2] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_park_2_2)

        pinBitmaps[PinKey.PIN_PARK_ZOOM_1] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_park_zoom_1)
        pinBitmaps[PinKey.PIN_PARK_ZOOM_2] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_park_zoom_2)
        pinBitmaps[PinKey.PIN_PARK_ZOOM_3] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_park_zoom_3)
        pinBitmaps[PinKey.PIN_PARK_ZOOM_4] = BitmapFactory.decodeResource(context.resources, R.drawable.pin_park_zoom_4)

        pinBitmaps[PinKey.BIKE_LOW] = context.getBitmapFromVectorDrawable(R.drawable.pin_bike_charge_low)
        pinBitmaps[PinKey.BIKE_MIDDLE] = context.getBitmapFromVectorDrawable(R.drawable.pin_bike_charge_mid)
        pinBitmaps[PinKey.BIKE_HIGH] = context.getBitmapFromVectorDrawable(R.drawable.pin_bike_charge_high)
        pinBitmaps[PinKey.PARKING] = context.getBitmapFromVectorDrawable(R.drawable.parking)

        pinBitmaps[PinKey.BIKE_IMAGE] = context.getBitmapFromVectorDrawable(R.drawable.pin_bicycle)
        pinBitmaps[PinKey.BIKE_IMAGE_EL] = context.getBitmapFromVectorDrawable(R.drawable.pin_bicycle_el)
        pinBitmaps[PinKey.BIKE_IMAGE_M] = context.getBitmapFromVectorDrawable(R.drawable.pin_bicycle_m)
        pinBitmaps[PinKey.PARK_IMAGE_EL] = context.getBitmapFromVectorDrawable(R.drawable.pin_parking_el)
        pinBitmaps[PinKey.PARK_IMAGE_M] = context.getBitmapFromVectorDrawable(R.drawable.pin_parking_m)
    }

    fun getPinBitmap(type: PinType, sizeEl: Int = 0, sizeM: Int = 0, batteryPower: Int = -1) : Bitmap {
        val lengthMax = if (max(sizeEl, sizeM) > 0) getText(max(sizeEl, sizeM)).length else 0
        val lengthMin = if (min(sizeEl, sizeM) > 0) getText(min(sizeEl, sizeM)).length else 0

        val pinKey = when {

            type == PinType.STATION_PARK && lengthMax == 1 && lengthMin == 0 -> PinKey.PIN_PARK_1

            type == PinType.STATION_PARK && lengthMax == 2 && lengthMin == 0 -> PinKey.PIN_PARK_2

            type == PinType.STATION_PARK && lengthMax == 1 && lengthMin == 1 -> PinKey.PIN_PARK_1_1

            type == PinType.STATION_PARK && lengthMax == 2 && lengthMin == 1 -> PinKey.PIN_PARK_2_1

            type == PinType.STATION_PARK && lengthMax == 2 && lengthMin == 2 -> PinKey.PIN_PARK_2_2

            type == PinType.STATION_PARK_ZOOM && lengthMax == 1 && lengthMin == 0 -> PinKey.PIN_PARK_ZOOM_1

            type == PinType.STATION_PARK_ZOOM && lengthMax == 2 && lengthMin == 0 -> PinKey.PIN_PARK_ZOOM_2

            type == PinType.STATION_PARK_ZOOM && lengthMax == 3 && lengthMin == 0 -> PinKey.PIN_PARK_ZOOM_3

            type == PinType.STATION_PARK_ZOOM && lengthMax == 4 && lengthMin == 0 -> PinKey.PIN_PARK_ZOOM_4

            (type == PinType.BIKE_ZOOM_SMALL || type == PinType.STATION) &&
            lengthMax == 1 && lengthMin == 0 -> PinKey.PIN_1

            (type == PinType.BIKE_ZOOM_SMALL || type == PinType.STATION) &&
            lengthMax == 2 && lengthMin == 0 -> PinKey.PIN_2

            type == PinType.STATION && lengthMax == 1 && lengthMin == 1 -> PinKey.PIN_1_1

            type == PinType.STATION && lengthMax == 2 && lengthMin == 1 -> PinKey.PIN_2_1

            type == PinType.STATION && lengthMax == 2 && lengthMin == 2 -> PinKey.PIN_2_2

            (type == PinType.BIKE_ZOOM || type == PinType.STATION_ZOOM) &&
            lengthMax == 1 && lengthMin == 0 -> PinKey.PIN_ZOOM_1

            (type == PinType.BIKE_ZOOM || type == PinType.STATION_ZOOM) &&
            lengthMax == 2 && lengthMin == 0 -> PinKey.PIN_ZOOM_2

            (type == PinType.BIKE_ZOOM || type == PinType.STATION_ZOOM) &&
            lengthMax == 3 && lengthMin == 0 -> PinKey.PIN_ZOOM_3

            (type == PinType.BIKE_ZOOM || type == PinType.STATION_ZOOM) &&
            lengthMax == 4 && lengthMin == 0 -> PinKey.PIN_ZOOM_4

            type == PinType.BIKE && batteryPower < BIKE_BATTERY_POWER_LOW -> PinKey.BIKE_LOW

            type == PinType.BIKE && batteryPower > BIKE_BATTERY_POWER_HIGH -> PinKey.BIKE_HIGH

            type == PinType.BIKE -> PinKey.BIKE_MIDDLE

            type == PinType.PARKING -> PinKey.PARKING

            type == PinType.BIKE_IMAGE -> PinKey.BIKE_IMAGE

            type == PinType.BIKE_IMAGE_EL -> PinKey.BIKE_IMAGE_EL

            type == PinType.BIKE_IMAGE_M -> PinKey.BIKE_IMAGE_M

            else -> PinKey.PIN_1
        }

        val pinBitmap = pinBitmaps[pinKey]!!

        return when(type) {
            PinType.BIKE, PinType.PARKING -> pinBitmap
            PinType.BIKE_ZOOM_SMALL, PinType.BIKE_ZOOM ->
                drawBikeZoomBitmap(type, sizeEl, pinBitmap)
            PinType.STATION, PinType.STATION_ZOOM ->
                drawStationBitmap(type, sizeEl, sizeM, pinBitmap)
            PinType.STATION_PARK, PinType.STATION_PARK_ZOOM ->
                drawStationParkBitmap(type, sizeEl, sizeM, pinBitmap)
            else -> pinBitmap
        }
    }

    private fun getText(size: Int) : String {
        return when {
            size < 0 -> "0"
            size < 50 -> size.toString()
//            size < 10 -> size.toString()
//            size in 10..49 -> "10+"
            size in 50..99 -> "50+"
            else -> "100+"
//            size in 100..199 -> "100+"
//            size in 200..999 -> "200+"
//            else -> "1000+"
        }
    }

    private fun drawBikeZoomBitmap(type: PinType, size: Int, pinBitmap: Bitmap) : Bitmap {
        val text = getText(size)
        val isZoom = type == PinType.BIKE_ZOOM
        val clusterBitmap = pinBitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(clusterBitmap)

        if (!isZoom) {
            val left = 12.dpToPx(context).toFloat()
            val top = 8.dpToPx(context).toFloat()
            canvas.drawBitmap(pinBitmaps[PinKey.BIKE_IMAGE]!!, left, top, null)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = context.getColor(R.color.pin_bike_counter)

        val left = (if (isZoom) 9 else 40).dpToPx(context).toFloat()
        val top = (if (isZoom) 9 else 10).dpToPx(context).toFloat()
        val width = (numberSizes[text.length - 1]).dpToPx(context).toFloat()
        val height = 20.dpToPx(context).toFloat()
        val r = 16.dpToPx(context).toFloat()
        canvas.drawRoundRect(left, top, left + width, top + height, r, r, paint)

        paint.color = Color.WHITE
        val scale: Float = context.resources.displayMetrics.density
        paint.textSize = TEXT_SIZE * scale
        paint.typeface = Typeface.DEFAULT_BOLD
//        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)

        val x = left + 6.dpToPx(context).toFloat()
        val y = top + 15.dpToPx(context).toFloat()
        canvas.drawText(text, x, y, paint)
        return clusterBitmap
    }

    private fun drawStationBitmap(type: PinType, sizeEl: Int = 0, sizeM: Int = 0, pinBitmap: Bitmap) : Bitmap {
        val textEl = if (sizeEl > 0) getText(sizeEl) else ""
        val textM = if (sizeM > 0) getText(sizeM) else ""
        val isElectro = textEl.isNotEmpty()
        val isZoom = type == PinType.STATION_ZOOM
        val clusterBitmap = pinBitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(clusterBitmap)

        if (!isZoom) {
            val left = 12.dpToPx(context).toFloat()
            val top = 8.dpToPx(context).toFloat()
            val pinKey = if (isElectro) PinKey.BIKE_IMAGE_EL else PinKey.BIKE_IMAGE_M
            canvas.drawBitmap(pinBitmaps[pinKey]!!, left, top, null)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = if (isElectro) context.getColor(R.color.pin_bike_el_counter)
                      else context.getColor(R.color.pin_bike_m_counter)
        val text1 = if (textEl.isNotEmpty()) textEl else if (textM.isNotEmpty()) textM else "0"
        val left = (if (isZoom) 9 else 40).dpToPx(context).toFloat()
        val top = (if (isZoom) 9 else 10).dpToPx(context).toFloat()
        val width = (numberSizes[text1.length - 1]).dpToPx(context).toFloat()
        val height = 20.dpToPx(context).toFloat()
        val r = 16.dpToPx(context).toFloat()
        canvas.drawRoundRect(left, top, left + width, top + height, r, r, paint)

        paint.color = Color.WHITE
        val scale: Float = context.resources.displayMetrics.density
        paint.textSize = TEXT_SIZE * scale
        paint.typeface = Typeface.DEFAULT_BOLD
//        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)

        val x = left + 6.dpToPx(context).toFloat()
        val y = top + 15.dpToPx(context).toFloat()
        canvas.drawText(text1, x, y, paint)

        if (textEl.isNotEmpty() && textM.isNotEmpty()) {
            paint.color = context.getColor(R.color.pin_bike_m_counter)
            val left2 = left + width + 2.dpToPx(context).toFloat()
            val width2 = (numberSizes[textM.length - 1]).dpToPx(context).toFloat()
            canvas.drawRoundRect(left2, top, left2 + width2, top + height, r, r, paint)

            paint.color = Color.WHITE
            val x2 = left2 + 6.dpToPx(context).toFloat()
            canvas.drawText(textM, x2, y, paint)
        }

        return clusterBitmap
    }

    private fun drawStationParkBitmap(type: PinType, sizeEl: Int = 0, sizeM: Int = 0, pinBitmap: Bitmap) : Bitmap {
        val textEl = if (sizeEl > 0) getText(sizeEl) else ""
        val textM = if (sizeM > 0) getText(sizeM) else ""
        val isElectro = textEl.isNotEmpty()
        val isZoom = type == PinType.STATION_PARK_ZOOM
        val clusterBitmap = pinBitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(clusterBitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = if (isElectro) context.getColor(R.color.pin_bike_el_counter)
        else context.getColor(R.color.pin_bike_m_counter)
        val text1 = if (textEl.isNotEmpty()) textEl else if (textM.isNotEmpty()) textM else "0"
        val left = 8.dpToPx(context).toFloat()
        val top = 7.dpToPx(context).toFloat()
        val width = (numberSizes[text1.length - 1] + if (isZoom) -1 else 20).dpToPx(context).toFloat()
        val height = (if (isZoom) 21 else 23).dpToPx(context).toFloat()
        val r = 4.dpToPx(context).toFloat()
        canvas.drawRoundRect(left, top, left + width, top + height, r, r, paint)

        if (!isZoom) {
            val pinKey = if (isElectro) PinKey.PARK_IMAGE_EL else PinKey.PARK_IMAGE_M
            canvas.drawBitmap(pinBitmaps[pinKey]!!, left + 3.dpToPx(context).toFloat(), top + 2.dpToPx(context).toFloat(), null)
        }

        paint.color = Color.WHITE
        val scale: Float = context.resources.displayMetrics.density
        paint.textSize = TEXT_SIZE * scale
        paint.typeface = Typeface.DEFAULT_BOLD
//        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)

        val x = left + (if (isZoom) 6 else 26).dpToPx(context).toFloat()
        val y = top + (if (isZoom) 15 else 17).dpToPx(context).toFloat()
        canvas.drawText(text1, x, y, paint)

        if (textEl.isNotEmpty() && textM.isNotEmpty()) {
            paint.color = context.getColor(R.color.pin_bike_m_counter)
            val left2 = left + width + 3.dpToPx(context).toFloat()
            val width2 = (numberSizes[textM.length - 1] + if (isZoom) 8 else 20).dpToPx(context).toFloat()
            canvas.drawRoundRect(left2, top, left2 + width2, top + height, r, r, paint)

            if (!isZoom) {
                canvas.drawBitmap(pinBitmaps[PinKey.PARK_IMAGE_M]!!, left2 + 3.dpToPx(context).toFloat(), top + 2.dpToPx(context).toFloat(), null)
            }

            paint.color = Color.WHITE
            val x2 = left2 + (if (isZoom) 9 else 26).dpToPx(context).toFloat()
            canvas.drawText(textM, x2, y, paint)
        }

        return clusterBitmap
    }

    companion object {
        private const val TEXT_SIZE = 14
    }
}