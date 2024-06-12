package com.tomclaw.appsend.analytics

import com.tomclaw.appsend.util.md5
import java.io.File

fun generateEventFileName(event: String, time: Long): String {
    return time.toString() + "-" + event.md5() + ".event"
}

fun getFileNameTime(fileName: String): Long {
    val timeDivider = fileName.indexOf('-')
    return if (timeDivider > 0) {
        fileName.substring(0, timeDivider).toLong()
    } else 0
}

class EventFileComparator : Comparator<File> {

    override fun compare(o1: File, o2: File): Int {
        return compare(getFileNameTime(o1.name), getFileNameTime(o2.name))
    }

    private fun compare(x: Long, y: Long): Int {
        return if (x < y) -1 else if (x == y) 0 else 1
    }

}
