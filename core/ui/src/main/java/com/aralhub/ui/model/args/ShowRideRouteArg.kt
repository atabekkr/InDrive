package com.aralhub.ui.model.args

import android.os.Parcelable
import com.aralhub.ui.model.ClientRideLocationsUI
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShowRideRouteArg(
    val locations: List<ClientRideLocationsUI>,
): Parcelable
