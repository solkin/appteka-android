package com.tomclaw.appsend.screen.profile

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ProfileDeepLinkParserTest(
    private val host: String?,
    private val pathSegments: List<String>?,
    private val expected: ProfileDeepLink
) {

    @Test
    fun parseDeepLinkTest() {
        val result = ProfileDeepLinkParserImpl.parse(host, pathSegments)
        assertEquals(expected, result)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "host={0}, path={1} -> {2}")
        fun data(): Collection<Array<Any?>> {
            return listOf(
                // Valid profile URLs
                arrayOf(
                    "appteka.store",
                    listOf("profile", "123"),
                    ProfileDeepLink.ByUserId(123)
                ),
                arrayOf(
                    "appteka.store",
                    listOf("profile", "1"),
                    ProfileDeepLink.ByUserId(1)
                ),
                arrayOf(
                    "appteka.store",
                    listOf("profile", "999999"),
                    ProfileDeepLink.ByUserId(999999)
                ),

                // Valid user URLs
                arrayOf(
                    "appteka.store",
                    listOf("user", "123"),
                    ProfileDeepLink.ByUserId(123)
                ),
                arrayOf(
                    "appteka.store",
                    listOf("user", "456"),
                    ProfileDeepLink.ByUserId(456)
                ),

                // Invalid: wrong host
                arrayOf(
                    "appsend.store",
                    listOf("profile", "123"),
                    ProfileDeepLink.Invalid
                ),
                arrayOf(
                    "appteka.org",
                    listOf("profile", "123"),
                    ProfileDeepLink.Invalid
                ),
                arrayOf(
                    "example.com",
                    listOf("profile", "123"),
                    ProfileDeepLink.Invalid
                ),

                // Invalid: null host
                arrayOf<Any?>(
                    null,
                    listOf("profile", "123"),
                    ProfileDeepLink.Invalid
                ),

                // Invalid: wrong path structure - too few segments
                arrayOf(
                    "appteka.store",
                    listOf("profile"),
                    ProfileDeepLink.Invalid
                ),
                arrayOf(
                    "appteka.store",
                    emptyList<String>(),
                    ProfileDeepLink.Invalid
                ),

                // Invalid: wrong path structure - too many segments
                arrayOf(
                    "appteka.store",
                    listOf("profile", "123", "extra"),
                    ProfileDeepLink.Invalid
                ),

                // Invalid: non-numeric user ID
                arrayOf(
                    "appteka.store",
                    listOf("profile", "abc"),
                    ProfileDeepLink.Invalid
                ),
                arrayOf(
                    "appteka.store",
                    listOf("profile", ""),
                    ProfileDeepLink.Invalid
                ),
                arrayOf(
                    "appteka.store",
                    listOf("user", "not-a-number"),
                    ProfileDeepLink.Invalid
                ),

                // Invalid: zero or negative user ID
                arrayOf(
                    "appteka.store",
                    listOf("profile", "0"),
                    ProfileDeepLink.Invalid
                ),
                arrayOf(
                    "appteka.store",
                    listOf("profile", "-1"),
                    ProfileDeepLink.Invalid
                ),

                // Invalid: unsupported path type
                arrayOf(
                    "appteka.store",
                    listOf("app", "123"),
                    ProfileDeepLink.Invalid
                ),
                arrayOf(
                    "appteka.store",
                    listOf("unknown", "123"),
                    ProfileDeepLink.Invalid
                ),

                // Invalid: null path
                arrayOf<Any?>(
                    "appteka.store",
                    null,
                    ProfileDeepLink.Invalid
                ),
            )
        }
    }
}
