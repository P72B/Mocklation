package de.p72b.mocklation.util

import de.p72b.mocklation.data.MockFeature
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.linearref.LengthLocationMap
import org.locationtech.jts.linearref.LocationIndexedLine

class StatisticsCalculator(private var feature: MockFeature) {
    private lateinit var path: LineString
    lateinit var lengthLocationMap: LengthLocationMap
    lateinit var locationIndexedLine: LocationIndexedLine

    //private val lengthIndexedLine = LengthIndexedLine(path)
    var totalTravelTimeInSeconds: Double = 0.0
    var avgSpeedInKmh: Double = 0.0
    var pathLengthInMeter: Double = 0.0

    init {
        doCalculation()
    }

    fun setFeature(value: MockFeature) {
        feature = value
        doCalculation()
    }

    private fun doCalculation() {
        if (feature.nodes.size <= 1) {
            totalTravelTimeInSeconds = 0.0
            avgSpeedInKmh = 0.0
            pathLengthInMeter = 0.0
            return
        }
        val newPath = feature.nodes.convertToLineString()
        path = newPath
        lengthLocationMap = LengthLocationMap(newPath)
        locationIndexedLine = LocationIndexedLine(newPath)
        pathLengthInMeter = newPath.length * 100_000
        totalTravelTimeInSeconds = 0.0

        for (i in feature.nodes.indices) {
            if (i == feature.nodes.size - 1) {
                continue
            }
            val next = feature.nodes[i + 1]
            val distanceToNextInMeter = feature.nodes[i].distance(next) * 100_000
            val sectionTimeInSeconds = distanceToNextInMeter / (feature.nodes[i].speedInKmh / 3.6)
            totalTravelTimeInSeconds += sectionTimeInSeconds
        }
        avgSpeedInKmh = pathLengthInMeter / totalTravelTimeInSeconds
    }
}