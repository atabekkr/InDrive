package com.aralhub.ui.model.args

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedLocations(
    var from: SelectedLocation,
    var to: SelectedLocation
) : Parcelable

@Parcelize
data class SelectedLocation(
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val locationType: LocationType
) : Parcelable

enum class LocationType {
    FROM, TO
}
