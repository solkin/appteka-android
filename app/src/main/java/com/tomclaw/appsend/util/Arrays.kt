package com.tomclaw.appsend.util

import android.os.Parcel
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

fun ByteArray.unmarshallToParcel(): Parcel {
    val parcel = Parcel.obtain()
    parcel.unmarshall(this, 0, size)
    parcel.setDataPosition(0)
    return parcel
}

fun ByteArray.zip(): ByteArray {
    val deflater = Deflater()
    deflater.setInput(this)

    val estimatedCompressedSize = (size / 2)
    val output = ByteArrayOutputStream(estimatedCompressedSize)
    deflater.finish()
    val buffer = ByteArray(BUFFER_SIZE)
    while (!deflater.finished()) {
        val count = deflater.deflate(buffer)
        output.write(buffer, 0, count)
    }
    output.close()
    deflater.end()
    return output.toByteArray()
}

fun ByteArray.unzip(): ByteArray {
    val inflater = Inflater()
    inflater.setInput(this)

    val output = ByteArrayOutputStream(size)
    val buffer = ByteArray(BUFFER_SIZE)
    while (!inflater.finished()) {
        val count = inflater.inflate(buffer)
        output.write(buffer, 0, count)
    }
    output.close()
    inflater.end()
    return output.toByteArray()
}

private const val BUFFER_SIZE = 1024
