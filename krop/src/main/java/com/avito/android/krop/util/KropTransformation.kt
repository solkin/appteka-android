package com.avito.android.krop.util

import android.graphics.PointF
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class KropTransformation(
        val scale: Float,
        val focusOffset: PointF,
        val rotationAngle: Float = 0f
) : Parcelable