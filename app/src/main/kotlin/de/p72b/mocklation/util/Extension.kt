package de.p72b.mocklation.util

import java.math.RoundingMode

fun Double.roundTo(digitPlaces: Int = 2): Double {
    return this.toBigDecimal().setScale(digitPlaces, RoundingMode.HALF_EVEN).toDouble()
}