package de.p72b.mocklation.data

import android.location.Location

data class WayPoint(
    val speedInKmh: Int,
    val location: Location,
    var isTunnel: Boolean = false
)