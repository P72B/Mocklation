package de.p72b.mocklation.service.location.sampler


import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import de.p72b.mocklation.data.WayPoint
import de.p72b.mocklation.util.Logger
import java.util.Date

interface LocationSimulationSampler {
    fun pause()
    fun resume()
    fun getNextInstruction(): Instruction
}

object SamplerBuilder {
    fun create(wayPoints: List<WayPoint>): LocationSimulationSampler {
        return if (wayPoints.size == 1) {
            FixedLocationSimulationSampler(wayPoints.first())
        } else {
            RouteLocationSimulationSampler(wayPoints)
        }
    }
}

open class SimulationSampler(
    val useExactLocation: Boolean = false,
    private val considerTunnel: Boolean = true
) {
    var startTimeInMillis: Long = -1
    var nowInMillis: Long = 0
    var elapsedTimeInMillis: Long = 0
    private var pauseStartTimeInMillis: Long = -1
    private var elapsedPauseTimeInMillis: Long = 0
    var totalElapsedPauseTimeInMillis: Long = 0
    var isPaused: Boolean = false

    fun updateClock() {
        nowInMillis = Date().time
        if (startTimeInMillis < 0) {
            startTimeInMillis = nowInMillis
            return
        }
        elapsedTimeInMillis = nowInMillis - startTimeInMillis

        if (isPaused) {
            if (pauseStartTimeInMillis < 0) {
                pauseStartTimeInMillis = nowInMillis
            } else {
                val delta = elapsedPauseTimeInMillis
                elapsedPauseTimeInMillis = nowInMillis - pauseStartTimeInMillis
                totalElapsedPauseTimeInMillis += elapsedPauseTimeInMillis - delta
            }
        } else {
            if (elapsedPauseTimeInMillis > 0) {
                elapsedPauseTimeInMillis = 0
            }
            pauseStartTimeInMillis = -1
        }
    }

    fun createLocationFrom(wayPoint: WayPoint): Location {
        return Location(LocationManager.GPS_PROVIDER).apply {
            latitude = wayPoint.location.latitude
            longitude = wayPoint.location.longitude
            accuracy = wayPoint.location.accuracy
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            time = nowInMillis
            speed = (wayPoint.speedInKmh / 3.6).toFloat()
        }
    }

    fun getLocationConsiderTunnel(
        location: Location,
        wayPoint: WayPoint
    ): Location? {
        if (considerTunnel.not()) {
            return location
        }
        return if (wayPoint.isTunnel) null else location
    }
}
sealed interface Instruction {
    data class FixedInstruction(
        val wayPoint: WayPoint,
        val location: Location?
    ): Instruction

    data class RouteInstruction(
        val wayPoint: WayPoint,
        val location: Location?,
        val totalTrackLengthInMeter: Double,
        val activeSectionIndex: Int,
        val totalSectionsIndex: Int,
        val isLast: Boolean = false,
        val progressInPercent: Double,
        val simulatedLengthInMeter: Double
    ): Instruction
}