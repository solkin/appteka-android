package com.tomclaw.appsend.util

import java.util.zip.CRC32

fun String.crc32(): Int {
    val crc32Calculator = CRC32()
    crc32Calculator.update(this.toByteArray())
    return crc32Calculator.value.toInt()
}
