package de.p72b.mocklation.service.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.parser.TrackImport
import de.p72b.mocklation.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class LocationSimulation(context: Context, feature: Feature) {

    private lateinit var job: Job
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val interval: Long = 1000
    private val sampler = LocationSimulationSampler(TrackImport(context), feature)

    fun run() {
        Logger.d(msg = "run")
        fusedLocationProviderClient.setMockMode(true)

        locationManager.addTestProvider(
            LocationManager.GPS_PROVIDER,
            false,
            false,
            false,
            false,
            true,
            true,
            true,
            2,
            1
        )
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)

        job = startRepeatingJob(interval)
    }

    fun stop() {
        job.cancel()
        locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        fusedLocationProviderClient.setMockMode(false)
    }

    /**
     * Works only for FusedLocationProviderClient
     */
    private fun getBestProvider(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            LocationManager.FUSED_PROVIDER
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER
        } else {
            return LocationManager.GPS_PROVIDER
        }
    }

    private fun startRepeatingJob(timeIntervalInMillis: Long): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val next = sampler.getNextInstruction()
                if (next.hasFinished()) {
                    Logger.d(msg = "simulation finished")
                    return@launch
                }
                next.getLocation()?.let {
                    publishMockLocation(next.getLocation()!!)
                }
                if (next.getLocation() == null) {
                    Logger.d(msg = "location is null")
                }
                delay(timeIntervalInMillis)
            }
        }
    }

    private fun publishMockLocation(newLocation: Location) {
        newLocation.provider = LocationManager.GPS_PROVIDER

        locationManager.setTestProviderLocation(
            LocationManager.GPS_PROVIDER,
            newLocation
        )

        newLocation.provider = getBestProvider()
        fusedLocationProviderClient.setMockLocation(newLocation)
            .addOnFailureListener { _ ->
                Logger.d(msg = "Did not worked")
            }
        Logger.d(msg = "publish mock location: ${newLocation.latitude}/${newLocation.longitude}")
    }

}