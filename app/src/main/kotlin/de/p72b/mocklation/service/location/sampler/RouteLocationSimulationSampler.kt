package de.p72b.mocklation.service.location.sampler

import android.location.Location
import android.location.LocationManager
import de.p72b.mocklation.data.MockFeature
import de.p72b.mocklation.util.StatisticsCalculator
import de.p72b.mocklation.util.applyRandomGpsNoice
import org.locationtech.jts.linearref.LinearLocation

class RouteLocationSimulationSampler(
    private val mockFeature: MockFeature,
    considerTunnel: Boolean = true
) : LocationSimulationSampler, SimulationSampler(considerTunnel) {
    private val stats = StatisticsCalculator(mockFeature)

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        isPaused = false
    }

    override fun getNextInstruction(): Instruction {
        super.updateClock()

        val distanceTraveledInMeter = stats.avgSpeedInKmh * ((elapsedTimeInMillis - totalElapsedPauseTimeInMillis) / 1_000)
        val linearLocation = stats.lengthLocationMap.getLocation(distanceTraveledInMeter / 100_000)
        val forwardShiftedCoordinate = stats.locationIndexedLine.extractPoint(linearLocation)
        val currentSectionStartWayPoint = mockFeature.nodes[linearLocation!!.segmentIndex]
        val mockLocation = createLocationFrom(currentSectionStartWayPoint)
        val location: LinearLocation = stats.locationIndexedLine.project(forwardShiftedCoordinate)
        val lengthAlongLine = stats.lengthLocationMap.getLength(location) * 100_000

        mockLocation.apply {
            this.latitude = forwardShiftedCoordinate.x
            this.longitude = forwardShiftedCoordinate.y
            this.bearing = Location(LocationManager.GPS_PROVIDER).apply {
                latitude = currentSectionStartWayPoint.geometry.latitude
                longitude = currentSectionStartWayPoint.geometry.longitude
            }.bearingTo(this)
        }

        if (mockLocation.accuracy != 0.0f) {
            mockLocation.applyRandomGpsNoice()
        }

        return Instruction.RouteInstruction(
            node = currentSectionStartWayPoint,
            totalTrackLengthInMeter = stats.pathLengthInMeter,
            activeSectionIndex = linearLocation.segmentIndex,
            totalSectionsIndex = mockFeature.nodes.size - 1,
            location = mockLocation,
            isLast = isLastWayPoint(distanceTraveledInMeter),
            progressInPercent = 100 * lengthAlongLine / stats.pathLengthInMeter,
            simulatedLengthInMeter = lengthAlongLine
        )
    }

    private fun isLastWayPoint(distanceTraveledInMeter: Double): Boolean {
        return distanceTraveledInMeter >= stats.pathLengthInMeter
    }
}