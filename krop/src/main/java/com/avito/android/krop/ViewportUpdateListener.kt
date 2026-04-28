package com.avito.android.krop

import android.graphics.RectF

interface ViewportUpdateListener {
    fun onUpdateViewport(newViewport: RectF)
}