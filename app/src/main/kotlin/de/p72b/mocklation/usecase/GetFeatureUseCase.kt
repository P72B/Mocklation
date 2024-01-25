package de.p72b.mocklation.usecase

import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.data.FeatureRepository
import de.p72b.mocklation.data.util.Resource

class GetFeatureUseCase(
    private val repository: FeatureRepository
) {
    suspend fun invoke(id: String): Resource<Feature> {
        return repository.findFeature(id)
    }
}