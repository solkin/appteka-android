package com.avito.android.krop.util

internal data class KPoint(val x: Float, val y: Float) {

    fun moveBy(dx: Float, dy: Float) = KPoint(x + dx, y + dy)
}