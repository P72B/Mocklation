package de.p72b.mocklation.data

import de.p72b.mocklation.data.mapper.FeatureMapper
import de.p72b.mocklation.data.room.FeatureDatabase
import de.p72b.mocklation.data.room.FeatureEntity
import de.p72b.mocklation.data.util.Resource
import de.p72b.mocklation.data.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeatureRepository(
    private val featureDatabase: FeatureDatabase,
    private val featureMapper: FeatureMapper
) {

    suspend fun getFeatureCollection(): Resource<List<Feature>> {
        return withContext(Dispatchers.IO) {
            Resource(status = Status.SUCCESS, data = mapFeatures(featureDatabase.featureDao().getAll()))
        }
    }

    private fun mapFeatures(input: List<FeatureEntity>): List<Feature> {
        return input.filter { it.name != null }.map {
            featureMapper.map(it)
        }
    }
}