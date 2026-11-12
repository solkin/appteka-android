package com.tomclaw.appsend.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class RecencyTest {

    private val now = 1_800_000_000_000L

    @Test
    fun `an hour ago is recent`() {
        assertTrue(isRecent(now - TimeUnit.HOURS.toMillis(1), now))
    }

    @Test
    fun `a week ago is not recent`() {
        assertFalse(isRecent(now - TimeUnit.DAYS.toMillis(7), now))
    }

    @Test
    fun `exactly a day ago is no longer recent`() {
        assertFalse(isRecent(now - TimeUnit.DAYS.toMillis(1), now))
    }

}
