package com.tomclaw.appsend.screen.post

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import com.tomclaw.appsend.util.decodeSampledBitmapFromStream
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
            val bytes = ByteArrayOutputStream()

            private fun getBytes(): ByteArray {
                if (bytes.size() > 0) {
                    return bytes.toByteArray()
                }
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = decodeSampledBitmapFromStream(
                        inputStream,
                        IMAGE_MAX_PIXELS
                    ) ?: throw IOException("unable to decode file")
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        IMAGE_JPEG_QUALITY,
                        bytes
                    )
                    bytes.flush()
                } ?: throw IOException("unable to read file")
                return bytes.toByteArray()
            }

            override fun contentType() = "image/jpeg".toMediaTypeOrNull()

            override fun contentLength() = getBytes().size.toLong()

            override fun writeTo(sink: BufferedSink) {
                sink.write(getBytes())
            }
        }
    }

}

const val IMAGE_MAX_PIXELS = 2000000
const val IMAGE_JPEG_QUALITY = 90
