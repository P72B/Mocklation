package de.p72b.mocklation.service.location

import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.data.WayPoint
import de.p72b.mocklation.parser.TrackImport
import java.util.Date

class LocationSimulationSampler(importer: TrackImport, feature: Feature) {
    private var track: MutableList<WayPoint> = mutableListOf()

    private var startTime: Long = -1
    private var currentWayPointIndex = 0

    init {
        feature.nodes.forEach {
            val location = Location(LocationManager.GPS_PROVIDER).apply {
                latitude = it.geometry.latLng.latitude
                longitude = it.geometry.latLng.longitude
                accuracy = 12f
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                time = Date().time
                speed = (60 / 3.6).toFloat()
            }
            track.add(WayPoint(speedInKmh = 60, location = location, isTunnel = false))
        }
        //track = importer.read()
    }

    fun getNextInstruction(): Instruction {
        val tstampMillis = Date().time
        if (startTime < 0) {
            startTime = tstampMillis
        }

        val current = track[currentWayPointIndex]
        val result = WayPoint(current.speedInKmh, Location(LocationManager.GPS_PROVIDER).apply {
            latitude = current.location.latitude
            longitude = current.location.longitude
            accuracy = current.location.accuracy
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            time = tstampMillis
            speed = (current.speedInKmh / 3.6).toFloat()
        }, isTunnel = current.isTunnel)

        if (track.size == 1 || startTime == tstampMillis) {
            return Instruction(result.getLocationConsiderTunnel())
        }

        val elapsedTimeInMillis = tstampMillis - startTime
        val nextIndex = currentWayPointIndex + 1
        if (track.size <= nextIndex) {
            return Instruction(
                result.getLocationConsiderTunnel(),
                currentWayPointIndex == track.size - 1
            )
        }

        val next = track[nextIndex]
        val distanceToNextWayPointInMeter = current.location.distanceTo(next.location)
        val distanceTraveledInMeter = (current.speedInKmh / 3.6) * (elapsedTimeInMillis / 1000)
        val distanceTraveledFractionInPercent =
            distanceTraveledInMeter / distanceToNextWayPointInMeter

        //Logger.d(msg = "current[$currentWayPointIndex] next[$nextIndex] distance traveled: $distanceTraveledInMeter m of $distanceToNextWayPointInMeter m (${distanceTraveledFractionInPercent * 100} %)")

        if (distanceToNextWayPointInMeter - distanceTraveledInMeter <= 0) {
            currentWayPointIndex++
            startTime = tstampMillis
            result.location.apply {
                latitude = next.location.latitude
                longitude = next.location.longitude
                accuracy = next.location.accuracy
            }
        } else {
            val nextInterpolatedLatLng = SphericalUtil.interpolate(
                LatLng(
                    current.location.latitude,
                    current.location.longitude
                ), LatLng(
                    next.location.latitude,
                    next.location.longitude
                ), distanceTraveledFractionInPercent
            )
            val salt = (0 until current.location.accuracy.toInt() / 2).random()
            val pepper = (0 until 360).random()
            val randomNextInterpolatedLatLng = SphericalUtil.computeOffset(
                nextInterpolatedLatLng,
                salt.toDouble(),
                pepper.toDouble()
            )
            result.location.apply {
                latitude = randomNextInterpolatedLatLng.latitude
                longitude = randomNextInterpolatedLatLng.longitude
                accuracy = current.location.accuracy
                bearing = current.location.bearingTo(next.location)
            }
        }

        return Instruction(
            result.getLocationConsiderTunnel(),
            currentWayPointIndex == track.size - 1
        )
    }

    private fun WayPoint.getLocationConsiderTunnel(): Location? {
        return if (this.isTunnel) null else this.location
    }
}

class Instruction(private val location: Location?, private val isLast: Boolean = false) {
    fun hasFinished(): Boolean {
        return isLast
    }

    fun getLocation(): Location? {
        return location
    }
}