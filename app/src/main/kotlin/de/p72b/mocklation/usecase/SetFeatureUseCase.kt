package de.p72b.mocklation.usecase

import de.p72b.mocklation.data.MockFeature
import de.p72b.mocklation.data.FeatureRepository
import de.p72b.mocklation.data.util.Resource

class SetFeatureUseCase(
    private val repository: FeatureRepository
) {
    suspend fun invoke(feature: MockFeature): Resource<Unit> {
        return repository.insertFeature(feature)
    }
}