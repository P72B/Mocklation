package de.p72b.mocklation.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

val dec = DecimalFormat("###,###,###,###,###", DecimalFormatSymbols(Locale.ENGLISH))

fun getHumanReadableRepresentationOfDistanceMeters(valueInMeters: Double): String {
    val unitSystem = Locale.getDefault().getUnitSystem().name

    val displayedPathLength: String
    when (valueInMeters) {
        in 0.0..1_000.0 -> {
            displayedPathLength = "${valueInMeters.toInt()} m"
        }

        in 1_000.0..10_000.0 -> {
            val km = (valueInMeters / 1_000).roundTo(1)
            var kmDisplay: String = km.toString()
            if (km.rem(1).equals(0.0)) {
                kmDisplay = km.toInt().toString()
            }
            displayedPathLength = "$kmDisplay km"
        }

        in 10_000.0..1_000_000.0 -> {
            displayedPathLength = "${(valueInMeters / 1_000).toInt()} km"
        }

        else -> {
            val formattedNumber = dec.format(valueInMeters / 1_000).replace(",", ".")
            displayedPathLength = "$formattedNumber km"
        }
    }
    return displayedPathLength
}

fun getHumanReadableRepresentationOfTimeInMilliseconds(valueInSeconds: Double): String {
    val displayedTravelTime: String
    when (valueInSeconds) {
        in 0.0..60.0 -> {
            var seconds = valueInSeconds.toInt()
            if (seconds == 0) {
                seconds++
            }
            displayedTravelTime = "$seconds s"
        }

        else -> {
            val hours: Int = (valueInSeconds / 3600).toInt()
            var minutes: Int = ((valueInSeconds % 3600) / 60).toInt()
            when (hours) {
                0 -> {
                    displayedTravelTime = "${(valueInSeconds / 60).toInt()} min"
                }

                in 1..23 -> {
                    if (minutes == 0) {
                        minutes++
                    }
                    displayedTravelTime = "$hours h $minutes min"
                }

                else -> {
                    val days: Int = hours / 24
                    var hoursEachDay = hours % 24
                    if (hoursEachDay == 0) {
                        hoursEachDay++
                    }
                    displayedTravelTime = "$days d $hoursEachDay h"
                }
            }
        }
    }
    return displayedTravelTime
}