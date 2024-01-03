package de.p72b.mocklation.usecase

import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.data.FeatureRepository
import de.p72b.mocklation.data.util.Resource
import de.p72b.mocklation.data.util.Status

class SetFeatureUseCase(
    private val repository: FeatureRepository
) {
    suspend fun invoke(feature: Feature): Resource<Unit> {
        return repository.insertFeature(feature)
    }
}