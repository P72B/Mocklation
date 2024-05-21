package de.p72b.mocklation.usecase

import de.p72b.mocklation.data.util.Resource
import de.p72b.mocklation.data.util.Status
import de.p72b.mocklation.util.convertToMultiPoint
import de.p72b.mocklation.util.convertToPoints
import de.p72b.mocklation.util.geometryFactory
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.MultiPoint

class MapBoundsUseCase(
    private val getCollectionUseCase: GetCollectionUseCase
) {

    suspend fun invoke(): Resource<Geometry?> {
        val collection = getCollectionUseCase.invoke()
        when (collection.status) {
            Status.SUCCESS -> {

                collection.data?.let {
                    if (it.isEmpty()) {
                        return Resource(
                            status = Status.SUCCESS,
                            data = null
                        )
                    } else {
                        var allCoordinates: Array<Coordinate> = emptyArray()
                        for (feature in it) {
                            allCoordinates = allCoordinates.plus(feature.nodes.convertToMultiPoint().coordinates)
                        }
                        return Resource(
                            status = Status.SUCCESS,
                            data = MultiPoint(
                                allCoordinates.convertToPoints(),
                                geometryFactory
                            ).envelope
                        )
                    }
                }
            }

            Status.ERROR -> return Resource(
                status = Status.SUCCESS,
                data = null
            )
        }
        return Resource(
            status = Status.SUCCESS,
            data = null
        )
    }
}