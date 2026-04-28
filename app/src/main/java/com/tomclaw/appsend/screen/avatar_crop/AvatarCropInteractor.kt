package com.tomclaw.appsend.screen.avatar_crop

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.decodeSampledBitmapFromStream
import com.tomclaw.cache.DiskLruCache
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

interface AvatarCropInteractor {

    /**
     * Decodes the picked image from `uri` on a worker thread, downsampled
     * to a screen-friendly size and rotated according to its EXIF
     * orientation so the cropper sees the picture upright.
     */
    fun loadBitmap(uri: Uri): Single<Bitmap>

    /**
     * Encodes [bitmap] as JPEG and stores the result in the picked-media
     * disk LRU cache under [cacheKey], returning the cached file's URI so
     * the caller can hand it back to the editor without keeping a hard
     * reference to a temp path. The cache eviction policy bounds the
     * transient footprint on its own.
     */
    fun saveBitmap(bitmap: Bitmap, cacheKey: String): Single<Uri>

}

class AvatarCropInteractorImpl(
    private val context: Context,
    private val contentResolver: ContentResolver,
    private val pickedMediaCache: DiskLruCache,
    private val schedulers: SchedulersFactory,
) : AvatarCropInteractor {

    override fun loadBitmap(uri: Uri): Single<Bitmap> = Single
        .fromCallable {
            val orientation = readExifOrientation(uri)
            val raw = contentResolver.openInputStream(uri)?.use { stream ->
                decodeSampledBitmapFromStream(stream, IMAGE_MAX_PIXELS)
            } ?: throw IOException("unable to read $uri")
            applyExifOrientation(raw, orientation)
        }
        .subscribeOn(schedulers.io())

    override fun saveBitmap(bitmap: Bitmap, cacheKey: String): Single<Uri> = Single
        .fromCallable {
            val capped = capToMaxDimension(bitmap, MAX_OUTPUT_DIMENSION)
            // Materialise the JPEG into a short-lived temp file, then
            // hand it to the disk LRU cache. The cache moves the bytes
            // into its own directory and returns the canonical File for
            // the entry; we delete the temp on the way out in case the
            // cache implementation copied rather than moved.
            val temp = File.createTempFile("avatar_crop_", ".jpg", context.cacheDir)
            try {
                FileOutputStream(temp).use { out ->
                    capped.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                    out.flush()
                }
                val cached = pickedMediaCache.put(cacheKey, temp)
                Uri.fromFile(cached)
            } finally {
                if (temp.exists()) temp.delete()
                if (capped !== bitmap) capped.recycle()
            }
        }
        .subscribeOn(schedulers.io())

    private fun capToMaxDimension(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val largest = maxOf(bitmap.width, bitmap.height)
        if (largest <= maxDimension) return bitmap
        val ratio = maxDimension.toFloat() / largest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true,
        )
    }

    private fun readExifOrientation(uri: Uri): Int = try {
        contentResolver.openInputStream(uri)?.use { input ->
            ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
    } catch (ignored: Throwable) {
        ExifInterface.ORIENTATION_NORMAL
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
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

}

private const val IMAGE_MAX_PIXELS = 4_000_000
private const val MAX_OUTPUT_DIMENSION = 1024
private const val JPEG_QUALITY = 90
