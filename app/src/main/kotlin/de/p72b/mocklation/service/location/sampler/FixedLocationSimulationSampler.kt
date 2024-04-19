package de.p72b.mocklation.service.location.sampler

import de.p72b.mocklation.data.WayPoint
import de.p72b.mocklation.util.applyRandomGpsNoice

class FixedLocationSimulationSampler(
    private val wayPoint: WayPoint,
    useExactLocation: Boolean = false
): LocationSimulationSampler, SimulationSampler(useExactLocation)  {

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        isPaused = false
    }

    override fun getNextInstruction(): Instruction {
        super.updateClock()
        val mockLocation = createLocationFrom(wayPoint)

        if (isPaused.not() && useExactLocation.not()) {
            mockLocation.applyRandomGpsNoice()
        }

        return Instruction.FixedInstruction(
            wayPoint = wayPoint,
            location = getLocationConsiderTunnel(mockLocation, wayPoint)
        )
    }
}