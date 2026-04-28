package com.avito.android.krop.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.WorkerThread
import kotlinx.parcelize.Parcelize

data class BitmapTransformation(
        val transformationMatrix: Matrix = Matrix(),
        val inputSize: Size = Size(),
        val outputSize: Size = Size(),
) : Parcelable {

    constructor(parcel: Parcel) : this(
            Matrix().apply { setValues(parcel.createFloatArray() ?: FloatArray(GRAPHICS_MATRIX_SIZE)) },
            parcel.readParcelable<Size>(Size::class.java.classLoader) ?: Size(),
            parcel.readParcelable<Size>(Size::class.java.classLoader) ?: Size()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        val matrixArray = FloatArray(GRAPHICS_MATRIX_SIZE)
        transformationMatrix.getValues(matrixArray)

        parcel.writeFloatArray(matrixArray)
        parcel.writeParcelable(inputSize, flags)
        parcel.writeParcelable(outputSize, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BitmapTransformation> {
        override fun createFromParcel(parcel: Parcel): BitmapTransformation {
            return BitmapTransformation(parcel)
        }

        override fun newArray(size: Int): Array<BitmapTransformation?> {
            return arrayOfNulls(size)
        }
    }

    @Parcelize
    data class Size(val width: Int = 0, val height: Int = 0) : Parcelable
}

private const val GRAPHICS_MATRIX_SIZE = 9

/**
 * @receiver input bitmap to make transformation
 * @param transformation object, containing service info with changes
 *
 * @return specific part of source bitmap, after applying given transformation
 * @throws IllegalStateException when transformation object is used on wrong bitmap
 */
@WorkerThread
fun Bitmap.transformWith(transformation: BitmapTransformation): Bitmap {
    check(transformation.inputSize == BitmapTransformation.Size(width, height)) {
        "Transformation is intended for bitmap size ${transformation.inputSize}"
    }

    val (width, height) = with(transformation.outputSize) { width to height }
    val result = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    canvas.drawBitmap(this, transformation.transformationMatrix, null)
    return result
}
