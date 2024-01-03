package de.p72b.mocklation.data.mapper

import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.data.room.FeatureEntity

class FeatureMapper : Mapper<FeatureEntity, Feature> {
    override fun map(input: FeatureEntity): Feature =
        Feature(
            uuid = input.uid,
            name = input.name,
            nodes = input.nodes,
            lastModified = input.lastModified,
        )
}
class FeatureEntityMapper : Mapper<Feature, FeatureEntity> {
    override fun map(input: Feature): FeatureEntity =
        FeatureEntity(
            uid = input.uuid,
            name = input.name,
            nodes = input.nodes,
            lastModified = input.lastModified,
        )
}