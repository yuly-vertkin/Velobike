package ru.sitronics.velobike.tools

import android.graphics.Bitmap
import com.yandex.runtime.image.ImageProvider
import ru.sitronics.velobike.COMPACT_CLUSTERS_ZOOM
import java.util.UUID

class ClusterImageProvider (
    private val size: Int,
    private val zoom: Float,
    private val pinManager: PinManager,
    private val isParkMode: Boolean,
    private val type: ClusterType,
) : ImageProvider() {

    override fun getId() : String =
        "ClusterImageProvider:" + UUID.randomUUID().toString()

    override fun getImage() : Bitmap =
        when(type) {
            ClusterType.BIKE -> pinManager.getPinBitmap(
                if (zoom > COMPACT_CLUSTERS_ZOOM) PinType.BIKE_ZOOM_SMALL else PinType.BIKE_ZOOM,
                size
            )
            // TODO: PinType.STATION_CLUSTER_PARK
            ClusterType.STATION -> pinManager.getPinBitmap(
                if (!isParkMode) PinType.STATION_ZOOM else PinType.STATION_PARK_ZOOM,
                0, size
            )
        }
}

enum class ClusterType {
    BIKE, STATION
}