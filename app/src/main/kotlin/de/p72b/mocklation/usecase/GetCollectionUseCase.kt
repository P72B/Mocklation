package de.p72b.mocklation.usecase

import de.p72b.mocklation.data.MockFeature
import de.p72b.mocklation.data.FeatureRepository
import de.p72b.mocklation.data.util.Resource

class GetCollectionUseCase(
    private val repository: FeatureRepository
) {
    suspend fun invoke(): Resource<List<MockFeature>> {
        return repository.getFeatureCollection()
    }
}