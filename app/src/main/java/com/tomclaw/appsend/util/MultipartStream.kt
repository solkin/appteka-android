package com.tomclaw.appsend.util

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.io.OutputStream

/**
 * Created by solkin on 13.01.15.
 */
class MultipartStream(
    private val outputStream: OutputStream,
    private val boundary: String
) : OutputStream() {

    private var isSetLast = false
    private var isSetFirst = false

    fun writeFirstBoundaryIfNeeds() {
        if (!isSetFirst) {
            try {
                outputStream.write("--$boundary\r\n".toByteArray())
            } catch (e: IOException) {
                Log.e(TAG, e.message, e)
            }
        }
        isSetFirst = true
    }

    fun writeLastBoundaryIfNeeds() {
        if (isSetLast) {
            return
        }
        try {
            outputStream.write("\r\n--$boundary--\r\n".toByteArray())
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
        }
        isSetLast = true
    }

    fun writePart(key: String, value: String) {
        writeFirstBoundaryIfNeeds()
        try {
            outputStream.write("Content-Disposition: form-data; name=\"$key\"\r\n".toByteArray())
            outputStream.write("Content-Type: text/plain; charset=UTF-8\r\n".toByteArray())
            outputStream.write("Content-Transfer-Encoding: 8bit\r\n\r\n".toByteArray())
            outputStream.write(value.toByteArray())
            outputStream.write("\r\n--$boundary\r\n".toByteArray())
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
        }
    }

    fun writePart(
        key: String,
        fileName: String,
        inputStream: InputStream,
        type: String,
        callback: ProgressHandler
    ) {
        writePart(key, fileName, writer = { outputStream ->
            inputStream.use { stream ->
                var cache: Int
                var sent: Long = 0
                val buffer = ByteArray(BUFFER_SIZE)
                while (stream.read(buffer, 0, BUFFER_SIZE).also { cache = it } != -1) {
                    outputStream.write(buffer, 0, cache)
                    outputStream.flush()
                    sent += cache.toLong()
                    callback.onProgress(sent)
                    if (Thread.interrupted()) {
                        throw InterruptedException()
                    }
                }
            }
        }, type, callback)
    }

    fun writePart(
        key: String,
        fileName: String,
        writer: (OutputStream) -> (Unit),
        type: String,
        callback: ProgressHandler
    ) {
        var type = type
        writeFirstBoundaryIfNeeds()
        try {
            type = "Content-Type: $type\r\n"
            outputStream.write("Content-Disposition: form-data; name=\"$key\"; filename=\"$fileName\"\r\n".toByteArray())
            outputStream.write(type.toByteArray())
            outputStream.write("Content-Transfer-Encoding: binary\r\n\r\n".toByteArray())
            writer.invoke(outputStream)
            outputStream.write("\r\n--$boundary\r\n".toByteArray())
        } catch (ex: InterruptedIOException) {
            Log.e(TAG, "[upload] IO interruption while application downloading", ex)
            callback.onCancelled(ex)
        } catch (ex: InterruptedException) {
            Log.e(TAG, "[upload] Interruption while application downloading", ex)
            callback.onCancelled(ex)
        } catch (e: Throwable) {
            Log.e(TAG, e.message, e)
            callback.onError(e)
        }
    }

    @Throws(IOException::class)
    override fun write(oneByte: Int) {
        outputStream.write(oneByte)
    }

    @Throws(IOException::class)
    override fun flush() {
        outputStream.flush()
    }

    interface ProgressHandler {
        fun onProgress(sent: Long)
        fun onError(ex: Throwable)
        fun onCancelled(ex: Throwable)
    }

}

private const val TAG = "multipart"
private const val BUFFER_SIZE = 128 * 1024
