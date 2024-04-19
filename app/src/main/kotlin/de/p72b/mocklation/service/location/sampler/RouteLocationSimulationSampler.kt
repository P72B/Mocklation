package de.p72b.mocklation.service.location.sampler

import de.p72b.mocklation.data.WayPoint
import de.p72b.mocklation.util.applyRandomGpsNoice
import de.p72b.mocklation.util.convertToLineString
import de.p72b.mocklation.util.distance
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.linearref.LengthLocationMap
import org.locationtech.jts.linearref.LinearLocation
import org.locationtech.jts.linearref.LocationIndexedLine

class RouteLocationSimulationSampler(
    private val track: List<WayPoint>,
    useExactLocation: Boolean = false,
    considerTunnel: Boolean = true
) : LocationSimulationSampler, SimulationSampler(useExactLocation, considerTunnel) {
    private val path: LineString = track.convertToLineString()
    private val lengthLocationMap = LengthLocationMap(path)
    private val locationIndexedLine = LocationIndexedLine(path)
    //private val lengthIndexedLine = LengthIndexedLine(path)
    private var totalTravelTimeInSeconds = 0.0
    private var avgSpeedInKmh = 0.0
    private val pathLengthInMeter = path.length * 100_000

    init {
        for (i in track.indices) {
            if (i == track.size - 1) {
                continue
            }
            val next = track[i + 1]
            val distanceToNextInMeter = track[i].distance(next) * 100_000
            val sectionTimeInSeconds = distanceToNextInMeter / (track[i].speedInKmh / 3.6)
            totalTravelTimeInSeconds += sectionTimeInSeconds
        }
        avgSpeedInKmh = pathLengthInMeter / totalTravelTimeInSeconds
    }

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        isPaused = false
    }

    override fun getNextInstruction(): Instruction {
        super.updateClock()

        val distanceTraveledInMeter = avgSpeedInKmh * ((elapsedTimeInMillis - totalElapsedPauseTimeInMillis) / 1_000)
        val linearLocation = lengthLocationMap.getLocation(distanceTraveledInMeter / 100_000)
        val forwardShiftedCoordinate = locationIndexedLine.extractPoint(linearLocation)
        val currentSectionStartWayPoint = track[linearLocation.segmentIndex]
        val mockLocation = createLocationFrom(currentSectionStartWayPoint)
        val location: LinearLocation = locationIndexedLine.project(forwardShiftedCoordinate)
        val lengthAlongLine = lengthLocationMap.getLength(location) * 100_000

        mockLocation.apply {
            this.latitude = forwardShiftedCoordinate.x
            this.longitude = forwardShiftedCoordinate.y
        }

        if (useExactLocation.not()) {
            mockLocation.applyRandomGpsNoice()
        }

        return Instruction.RouteInstruction(
            wayPoint = currentSectionStartWayPoint,
            totalTrackLengthInMeter = pathLengthInMeter,
            activeSectionIndex = linearLocation.segmentIndex,
            totalSectionsIndex = track.size - 1,
            location = mockLocation,
            isLast = isLastWayPoint(distanceTraveledInMeter),
            progressInPercent = 100 * lengthAlongLine / pathLengthInMeter,
            simulatedLengthInMeter = lengthAlongLine
        )
    }

    private fun isLastWayPoint(distanceTraveledInMeter: Double): Boolean {
        return distanceTraveledInMeter >= pathLengthInMeter
    }
}