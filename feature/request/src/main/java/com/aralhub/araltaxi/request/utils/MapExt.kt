package com.aralhub.araltaxi.request.utils

import android.content.Context
import com.aralhub.araltaxi.core.common.utils.MapStyles
import com.aralhub.araltaxi.core.common.utils.loadJsonFromAssets
import com.yandex.mapkit.map.Map

fun Map.updateMapStyle(zoom: Float, context: Context) {
    val style = if (zoom >= MapStyles.SWITCH_ZOOM) {
        loadJsonFromAssets(context, MapStyles.STANDARD_MAP_STYLE)
    } else {
        loadJsonFromAssets(context, MapStyles.MINIMALISTIC_MAP_STYLE)
    }
    this.setMapStyle(style)
}
