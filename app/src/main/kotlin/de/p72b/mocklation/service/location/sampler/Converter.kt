package de.p72b.mocklation.service.location.sampler

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.data.WayPoint
import de.p72b.mocklation.parser.TrackImport
import java.util.Date

class Converter {
    companion object {
        fun fromRoom(feature: Feature): List<WayPoint> {
            val track = mutableListOf<WayPoint>()
            val speedInKmH = 120
            feature.nodes.forEach {
                val location = Location(LocationManager.GPS_PROVIDER).apply {
                    latitude = it.geometry.latLng.latitude
                    longitude = it.geometry.latLng.longitude
                    accuracy = 12f
                    elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                    time = Date().time
                    speed = (speedInKmH / 3.6).toFloat()
                }
                track.add(WayPoint(speedInKmh = speedInKmH, location = location, isTunnel = false))
            }
            return track
        }

        fun fromGeoJson(context: Context): List<WayPoint> {
            return TrackImport(context).read()
        }
    }
}