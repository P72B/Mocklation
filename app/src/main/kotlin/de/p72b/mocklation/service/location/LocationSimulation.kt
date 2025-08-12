package de.p72b.mocklation.service.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import de.p72b.mocklation.data.MockFeature
import de.p72b.mocklation.service.location.sampler.Instruction
import de.p72b.mocklation.service.location.sampler.LocationSimulationSampler
import de.p72b.mocklation.service.location.sampler.SamplerBuilder
import de.p72b.mocklation.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class LocationSimulation(
    context: Context,
    feature: MockFeature,
    private val listener: (SimulationState) -> Unit
) {
    private lateinit var job: Job
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val interval: Long = 1000
    private val sampler: LocationSimulationSampler =
        SamplerBuilder.create(feature)

    fun play() {
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

    fun pause() {
        sampler.pause()
    }

    fun resume() {
        sampler.resume()
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
                val instruction: Instruction = sampler.getNextInstruction()
                var isInsideTunnel = false
                val location: Location? = when (instruction) {
                    is Instruction.FixedInstruction -> instruction.location
                    is Instruction.RouteInstruction -> {
                        isInsideTunnel = instruction.node.isTunnel
                        instruction.location
                    }
                }
                if (instruction is Instruction.RouteInstruction && instruction.isLast) {
                    listener(SimulationState.Finished)
                    return@launch
                }
                if (isInsideTunnel.not()) {
                    location?.let {
                        publishMockLocation(it)
                    }
                }
                listener(SimulationState.Status(instruction))
                delay(timeIntervalInMillis)
            }
        }
    }

    private fun publishMockLocation(location: Location) {
        location.provider = LocationManager.GPS_PROVIDER
        locationManager.setTestProviderLocation(
            LocationManager.GPS_PROVIDER,
            location
        )

        location.provider = getBestProvider()
        fusedLocationProviderClient.setMockLocation(location)
            .addOnFailureListener { _ ->
                Logger.d(msg = "Did not worked")
            }
    }

    sealed interface SimulationState {
        data class Status(
            val instruction: Instruction
        ) : SimulationState

        data object Finished : SimulationState
    }

}