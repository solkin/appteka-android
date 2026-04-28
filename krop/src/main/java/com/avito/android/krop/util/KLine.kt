package com.avito.android.krop.util

import kotlin.math.abs
import kotlin.math.sqrt

internal data class KLine(val p1: KPoint, val p2: KPoint) {

    /**
     * https://en.wikipedia.org/wiki/Intersection_(Euclidean_geometry)#Two_line_segments
     */
    fun findIntersection(line2: KLine): KPoint? {
        val x1 = p1.x
        val y1 = p1.y
        val x2 = p2.x
        val y2 = p2.y
        val x3 = line2.p1.x
        val y3 = line2.p1.y
        val x4 = line2.p2.x
        val y4 = line2.p2.y
        val t = (y3 - y1 - (x3 - x1) * (y2 - y1) / (x2 - x1)) / ((x4 - x3) * (y2 - y1) / (x2 - x1) - (y4 - y3))
        if (t < 0 || t > 1) return null

        val s = ((x3 - x1) + t * (x4 - x3)) / (x2 - x1)
        if (s < 0 || s > 1) return null

        return KPoint(x1 + s * (x2 - x1), y1 + s * (y2 - y1))
    }

    fun length() = sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y))

    fun getTransition() = (p2.x - p1.x) to (p2.y - p1.y)

    fun nearestPointFor(target : KPoint): KPoint {
        val normalPoint = normalFrom(target).p2
        val len = length()
        val lenP1 = KLine(p1, normalPoint).length()
        val lenP2 = KLine(p2, normalPoint).length()
        if (abs(lenP1 + lenP2 - len) < NEAREST_POINT_CALC_EPS) return normalPoint
        return if (lenP1 > lenP2) p2 else p1
    }

    fun normalFrom(target: KPoint): KLine {
        val sideLength1 = KLine(p1, target).length()
        val sideLength2 = KLine(p2, target).length()
        val lineLength = length()

        val weight2 = (sideLength2 * sideLength2 + lineLength * lineLength - sideLength1 * sideLength1) / (2 * lineLength)
        val weight1 = lineLength - weight2
        val weight = weight1 / (weight1 + weight2)
        val destination = KPoint(p1.x + (p2.x - p1.x) * weight, p1.y + (p2.y - p1.y) * weight)
        return KLine(target, destination)
    }
}

private const val NEAREST_POINT_CALC_EPS = 1