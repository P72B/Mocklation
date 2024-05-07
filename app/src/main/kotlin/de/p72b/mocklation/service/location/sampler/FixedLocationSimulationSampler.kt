package de.p72b.mocklation.service.location.sampler

import de.p72b.mocklation.data.MockFeature
import de.p72b.mocklation.util.applyRandomGpsNoice

class FixedLocationSimulationSampler(
    mockFeature: MockFeature,
    useExactLocation: Boolean = false
): LocationSimulationSampler, SimulationSampler(useExactLocation)  {

    private val node = mockFeature.nodes.first()

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        isPaused = false
    }

    override fun getNextInstruction(): Instruction {
        super.updateClock()
        val mockLocation = createLocationFrom(node)

        if (isPaused.not() && node.accuracyInMeter != 0.0f) {
            mockLocation.applyRandomGpsNoice()
        }

        return Instruction.FixedInstruction(
            node = node,
            location = getLocationConsiderTunnel(mockLocation, node.isTunnel)
        )
    }
}