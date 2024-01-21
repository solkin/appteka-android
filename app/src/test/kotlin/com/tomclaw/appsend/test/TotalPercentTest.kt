package com.tomclaw.appsend.test

import com.tomclaw.appsend.upload.totalPercent
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TotalPercentTest(
    private val apkCount: Int,
    private val apkPercent: Int,
    private val scrCount: Int,
    private val scrPercent: Int,
    private val total: Int,
) {

    @Test
    fun totalPercentTest() {
        val result = totalPercent(apkCount, apkPercent, scrCount, scrPercent)

        assertEquals(total, result)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(0, 0, 0, 0, 0),
                arrayOf(0, 0, 2, 0, 0),
                arrayOf(0, 0, 2, 50, 50),
                arrayOf(1, 0, 2, 0, 0),
                arrayOf(1, 50, 2, 0, 40),
                arrayOf(1, 100, 2, 0, 80),
                arrayOf(1, 100, 2, 50, 90),
                arrayOf(1, 100, 2, 100, 100),
            )
        }
    }

}
