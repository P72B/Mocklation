package de.p72b.mocklation.data.mapper

import de.p72b.mocklation.data.MockFeature
import de.p72b.mocklation.data.room.FeatureEntity

class FeatureMapper : Mapper<FeatureEntity, MockFeature> {
    override fun map(input: FeatureEntity): MockFeature =
        MockFeature(
            uuid = input.uid,
            name = input.name,
            nodes = input.nodes,
            lastModified = input.lastModified,
        )
}
class FeatureEntityMapper : Mapper<MockFeature, FeatureEntity> {
    override fun map(input: MockFeature): FeatureEntity =
        FeatureEntity(
            uid = input.uuid,
            name = input.name,
            nodes = input.nodes,
            lastModified = input.lastModified,
        )
}