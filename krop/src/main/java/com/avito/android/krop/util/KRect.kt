package com.avito.android.krop.util

import kotlin.math.abs

internal data class KRect(val leftTop: KPoint, val rightTop: KPoint, val rightBottom: KPoint, val leftBottom: KPoint) {
    fun center() = KPoint(x = (rightTop.x + leftBottom.x) / 2, y = (rightTop.y + leftBottom.y) / 2)

    fun moveBy(dx: Float, dy: Float) = KRect(
            leftTop = leftTop.moveBy(dx, dy),
            rightTop = rightTop.moveBy(dx, dy),
            rightBottom = rightBottom.moveBy(dx, dy),
            leftBottom = leftBottom.moveBy(dx, dy)
    )

    fun clockwiseBorders() = listOf(
            KLine(leftTop, rightTop),
            KLine(rightTop, rightBottom),
            KLine(rightBottom, leftBottom),
            KLine(leftBottom, leftTop)
    )

    fun clockwiseHeights() = listOf(leftTop, rightTop, rightBottom, leftBottom)

    fun contains(other: KRect) = contains(other.leftTop) && contains(other.rightTop) &&
            contains(other.leftBottom) && contains(other.rightBottom)

    fun contains(p: KPoint): Boolean {
        fun triangleSquare(p1: KPoint, p2: KPoint, p3: KPoint) =
                abs((p2.x - p1.x) * (p3.y - p1.y) - (p3.x - p1.x) * (p2.y - p1.y)) / 2

        val square = triangleSquare(leftTop, rightTop, leftBottom) * 2
        val pointSquare = triangleSquare(leftTop, rightTop, p) +
                triangleSquare(leftTop, leftBottom, p) +
                triangleSquare(leftBottom, rightBottom, p) +
                triangleSquare(rightTop, rightBottom, p)
        return abs(square - pointSquare) < SQUARE_COMPARE_EPS
    }
}

private const val SQUARE_COMPARE_EPS = 10