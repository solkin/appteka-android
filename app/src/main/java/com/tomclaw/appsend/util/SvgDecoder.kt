package com.tomclaw.appsend.util

import android.graphics.Bitmap
import android.graphics.Picture
import com.caverock.androidsvg.SVG
import com.tomclaw.imageloader.core.Decoder
import com.tomclaw.imageloader.core.Result
import com.tomclaw.imageloader.util.BitmapResult
import java.io.File
import java.io.FileInputStream

/**
 * Decoder for SVG images using AndroidSVG library.
 * This decoder checks if the file contains SVG content and decodes it to Bitmap.
 * 
 * Implements com.tomclaw.imageloader.core.Decoder interface for SimpleImageLoader.
 */
class SvgDecoder : Decoder {

    override fun probe(file: File): Boolean {
        return try {
            FileInputStream(file).use { inputStream ->
                val buffer = ByteArray(128)
                val bytesRead = inputStream.read(buffer)
                
                if (bytesRead > 0) {
                    val content = String(buffer, 0, bytesRead, Charsets.UTF_8)
                    // Проверяем, является ли это SVG по содержимому
                    val lowerContent = content.lowercase()
                    lowerContent.contains("<svg") || 
                    (lowerContent.contains("<?xml") && lowerContent.contains("svg"))
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun decode(file: File, width: Int, height: Int): Result? {
        return try {
            FileInputStream(file).use { inputStream ->
                val svg = SVG.getFromInputStream(inputStream)
                val picture: Picture = svg.renderToPicture()
                
                // Определяем размеры для рендеринга
                val svgWidth = if (picture.width > 0) picture.width else width
                val svgHeight = if (picture.height > 0) picture.height else height
                
                // Если размеры не заданы, используем размеры из запроса или дефолтные
                val finalWidth = if (width > 0) width else svgWidth.coerceAtLeast(1)
                val finalHeight = if (height > 0) height else svgHeight.coerceAtLeast(1)
                
                val bitmap = picture.toBitmap(
                    Bitmap.Config.ARGB_8888,
                    finalWidth,
                    finalHeight
                )

                BitmapResult(bitmap)
            }
        } catch (e: Exception) {
            null
        }
    }

}

