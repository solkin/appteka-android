package com.tomclaw.appsend.util

import java.util.concurrent.TimeUnit

/**
 * Whether [timeMillis] falls within the last day: what the "new" badge marks.
 */
fun isRecent(timeMillis: Long, nowMillis: Long = System.currentTimeMillis()): Boolean {
    return nowMillis - timeMillis < TimeUnit.DAYS.toMillis(1)
}
