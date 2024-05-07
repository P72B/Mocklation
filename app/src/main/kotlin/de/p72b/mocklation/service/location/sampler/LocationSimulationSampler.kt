package de.p72b.mocklation.service.location.sampler


import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import de.p72b.mocklation.data.MockFeature
import de.p72b.mocklation.data.Node
import java.util.Date

interface LocationSimulationSampler {
    fun pause()
    fun resume()
    fun getNextInstruction(): Instruction
}

object SamplerBuilder {
    fun create(mockFeature: MockFeature): LocationSimulationSampler {
        return if (mockFeature.nodes.size == 1) {
            FixedLocationSimulationSampler(mockFeature)
        } else {
            RouteLocationSimulationSampler(mockFeature)
        }
    }
}

open class SimulationSampler(
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

    fun createLocationFrom(node: Node): Location {
        return Location(LocationManager.GPS_PROVIDER).apply {
            latitude = node.geometry.latitude
            longitude = node.geometry.longitude
            accuracy = node.accuracyInMeter
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            time = nowInMillis
            speed = (node.speedInKmh / 3.6).toFloat()
        }
    }

    fun getLocationConsiderTunnel(
        location: Location,
        isTunnel: Boolean
    ): Location? {
        if (considerTunnel.not()) {
            return location
        }
        return if (isTunnel) null else location
    }
}
sealed interface Instruction {
    data class FixedInstruction(
        val node: Node,
        val location: Location?
    ): Instruction

    data class RouteInstruction(
        val node: Node,
        val location: Location?,
        val totalTrackLengthInMeter: Double,
        val activeSectionIndex: Int,
        val totalSectionsIndex: Int,
        val isLast: Boolean = false,
        val progressInPercent: Double,
        val simulatedLengthInMeter: Double
    ): Instruction
}