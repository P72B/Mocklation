package de.p72b.mocklation.util

import android.icu.util.LocaleData
import android.icu.util.LocaleData.MeasurementSystem
import android.icu.util.ULocale
import android.os.Build
import java.math.RoundingMode
import java.util.Locale

fun Double.roundTo(digitPlaces: Int = 2): Double {
    return this.toBigDecimal().setScale(digitPlaces, RoundingMode.HALF_EVEN).toDouble()
}

fun Locale.getUnitSystem(): UnitSystem {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val unit = LocaleData.getMeasurementSystem(ULocale.getDefault())
        return when (unit) {
            MeasurementSystem.SI -> UnitSystem.IMPERIAL
            MeasurementSystem.US -> UnitSystem.METRIC
            MeasurementSystem.UK -> UnitSystem.METRIC
            else -> UnitSystem.METRIC
        }
    } else {
        return when (country.uppercase()) {
            "US" -> UnitSystem.IMPERIAL_US
            "GB", "MM", "LR" -> UnitSystem.IMPERIAL
            else -> UnitSystem.METRIC
        }
    }
}

enum class UnitSystem {
    METRIC,
    IMPERIAL,
    IMPERIAL_US
}