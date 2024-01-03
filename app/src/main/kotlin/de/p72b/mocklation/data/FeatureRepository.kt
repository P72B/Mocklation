package de.p72b.mocklation.data

import de.p72b.mocklation.data.mapper.FeatureEntityMapper
import de.p72b.mocklation.data.mapper.FeatureMapper
import de.p72b.mocklation.data.room.FeatureDatabase
import de.p72b.mocklation.data.room.FeatureEntity
import de.p72b.mocklation.data.util.ERROR_NOT_FOUND
import de.p72b.mocklation.data.util.Resource
import de.p72b.mocklation.data.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeatureRepository(
    private val featureDatabase: FeatureDatabase,
    private val featureMapper: FeatureMapper,
    private val featureEntityMapper: FeatureEntityMapper,
) {

    suspend fun getFeatureCollection(): Resource<List<Feature>> {
        return withContext(Dispatchers.IO) {
            Resource(
                status = Status.SUCCESS,
                data = mapEntityList2FeatureList(featureDatabase.featureDao().getAll())
            )
        }
    }

    suspend fun insertFeature(feature: Feature): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            Resource(
                status = Status.SUCCESS,
                data = featureDatabase.featureDao().insert(featureEntityMapper.map(feature))
            )
        }
    }

    suspend fun findFeature(id: Int): Resource<Feature> {
        return withContext(Dispatchers.IO) {
            featureDatabase.featureDao().findById(id).let {
                if (it == null) {
                    Resource(status = Status.ERROR, code = ERROR_NOT_FOUND)
                } else {
                    Resource(
                        status = Status.SUCCESS,
                        data = featureMapper.map(it)
                    )
                }
            }
        }
    }

    private fun mapEntityList2FeatureList(input: List<FeatureEntity>): List<Feature> {
        return input.map {
            featureMapper.map(it)
        }
    }
}