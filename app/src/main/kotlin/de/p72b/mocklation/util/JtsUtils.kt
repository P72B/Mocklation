package de.p72b.mocklation.util

import android.location.Location
import de.p72b.mocklation.data.WayPoint
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.PrecisionModel
import org.locationtech.jts.geom.impl.CoordinateArraySequence
import org.locationtech.jts.linearref.LengthLocationMap
import org.locationtech.jts.linearref.LinearLocation
import org.locationtech.jts.linearref.LocationIndexedLine
import org.locationtech.jts.operation.buffer.BufferOp
import org.locationtech.jts.operation.buffer.BufferParameters
import org.locationtech.jts.shape.random.RandomPointsBuilder

val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

fun List<WayPoint>.convertToLineString(): LineString {
    val coordinateList = arrayOfNulls<Coordinate>(this.size)
    for (i in this.indices) {
        coordinateList[i] =
            Coordinate(this[i].location.latitude, this[i].location.longitude)
    }
    return LineString(
        CoordinateArraySequence(coordinateList),
        geometryFactory
    )
}

fun WayPoint.convertToPoint(): Point {
    return this.location.convertToPoint()
}

fun WayPoint.distance(to: WayPoint): Double {
    return this.convertToPoint().distance(to.convertToPoint())
}

fun Location.convertToPoint(): Point {
    return geometryFactory.createPoint(Coordinate(this.latitude, this.longitude))
}

val randomPointsBuilder = RandomPointsBuilder(geometryFactory)
fun Location.applyRandomGpsNoice() {
    val buffer = BufferOp(this.convertToPoint())
    buffer.setEndCapStyle(BufferParameters.CAP_ROUND)
    val bufferGeometry = buffer.getResultGeometry(((this.accuracy / 2) / 100_000).toDouble())
    randomPointsBuilder.setExtent(bufferGeometry)
    randomPointsBuilder.setNumPoints(1)
    val randomGeometry = randomPointsBuilder.geometry
    this.longitude = randomGeometry.coordinate.y
    this.latitude = randomGeometry.coordinate.x
}

fun lengthAlongLine(line: LineString, point: Coordinate): Double {
    val locationIndexedLine = LocationIndexedLine(line)
    val location: LinearLocation = locationIndexedLine.project(point)
    return LengthLocationMap(line).getLength(location) * 100_000
}
