package com.tomclaw.appsend.test

import com.tomclaw.appsend.upload.mergeEmptyStrings
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class MergeStringsTest(
    private val valueA: List<String?>,
    private val valueB: List<String>?,
    private val expected: List<String>
) {

    @Test
    fun mergeEmptyStringsTest() {
        val result = mergeEmptyStrings(valueA, valueB)

        assertEquals(result.size, expected.size)
        for (i in result.indices) {
            assertEquals(result[i], expected[i])
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(listOf("a", "b", null, null), listOf("c", "d"), listOf("a", "b", "c", "d")),
                arrayOf(listOf("a", "b", null, "d"), listOf("c"), listOf("a", "b", "c", "d")),
                arrayOf(listOf("a", "b", "c", "d"), emptyList<String>(), listOf("a", "b", "c", "d")),
                arrayOf(listOf(null, null), listOf("a", "b"), listOf("a", "b")),
                arrayOf(listOf("a", "b", null, "d"), listOf("c", "d"), listOf("a", "b", "c", "d")),
                arrayOf(listOf("a", "b", null, null), listOf("c"), listOf("a", "b", "c")),
                arrayOf(listOf("a", "b", "c", "d"), listOf("a"), listOf("a", "b", "c", "d")),
                arrayOf(listOf("a", null, "c", null), listOf("b"), listOf("a", "b", "c")),
                arrayOf(listOf("a", null, "c", null), listOf("b", "d"), listOf("a", "b", "c", "d")),
            )
        }
    }

}
