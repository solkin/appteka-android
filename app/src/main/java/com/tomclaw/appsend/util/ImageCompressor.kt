package com.tomclaw.appsend.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.ByteArrayOutputStream
import java.io.IOException

interface ImageCompressor {
    fun asRequestBody(uri: Uri): RequestBody
}

class ImageCompressorImpl(
    private val contentResolver: ContentResolver
) : ImageCompressor {

    override fun asRequestBody(uri: Uri): RequestBody {
        return object : RequestBody() {
            var data: ByteArray? = null

            private fun getBytes(): ByteArray {
                val data = data ?: run {
                    val orientation = readExifOrientation(uri)
                    val bytes = ByteArrayOutputStream()
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val raw = decodeSampledBitmapFromStream(
                            inputStream,
                            IMAGE_MAX_PIXELS
                        ) ?: throw IOException("unable to decode file")
                        val oriented = applyExifOrientation(raw, orientation)
                        oriented.compress(
                            Bitmap.CompressFormat.JPEG,
                            IMAGE_JPEG_QUALITY,
                            bytes
                        )
                        bytes.flush()
                        if (oriented !== raw) raw.recycle()
                        oriented.recycle()
                    } ?: throw IOException("unable to read file")
                    bytes.toByteArray()
                }
                this.data = data
                return data
            }

            override fun contentType() = "image/jpeg".toMediaTypeOrNull()

            override fun contentLength() = getBytes().size.toLong()

            override fun writeTo(sink: BufferedSink) {
                sink.write(getBytes())
            }
        }
    }

    private fun readExifOrientation(uri: Uri): Int {
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (ignored: Throwable) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f); matrix.preScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f); matrix.preScale(-1f, 1f)
            }
            else -> return bitmap
        }
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }

}

const val IMAGE_MAX_PIXELS = 2000000
const val IMAGE_JPEG_QUALITY = 85
