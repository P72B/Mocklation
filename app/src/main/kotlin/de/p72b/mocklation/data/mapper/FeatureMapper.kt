package de.p72b.mocklation.data.mapper

import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.data.room.FeatureEntity

class FeatureMapper : Mapper<FeatureEntity, Feature> {
    override fun map(input: FeatureEntity): Feature =
        Feature(
            name = input.name
        )
}