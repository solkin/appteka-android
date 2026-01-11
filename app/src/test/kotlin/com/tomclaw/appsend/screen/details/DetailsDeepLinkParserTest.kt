package com.tomclaw.appsend.screen.details

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DetailsDeepLinkParserTest(
    private val host: String?,
    private val pathSegments: List<String>?,
    private val expected: DetailsDeepLink
) {

    @Test
    fun parseDeepLinkTest() {
        val result = DetailsDeepLinkParserImpl.parse(host, pathSegments)
        assertEquals(expected, result)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "host={0}, path={1} -> {2}")
        fun data(): Collection<Array<Any?>> {
            return listOf(
                // Valid app URLs
                arrayOf(
                    "appteka.store",
                    listOf("app", "abc123"),
                    DetailsDeepLink.ByAppId("abc123")
                ),
                arrayOf(
                    "appteka.store",
                    listOf("apps", "abc123"),
                    DetailsDeepLink.ByAppId("abc123")
                ),
                arrayOf(
                    "appteka.store",
                    listOf("app", "12345"),
                    DetailsDeepLink.ByAppId("12345")
                ),

                // Valid package URLs
                arrayOf(
                    "appteka.store",
                    listOf("package", "com.example.app"),
                    DetailsDeepLink.ByPackageName("com.example.app")
                ),

                // Invalid: wrong host
                arrayOf(
                    "appsend.store",
                    listOf("app", "abc123"),
                    DetailsDeepLink.Invalid
                ),
                arrayOf(
                    "play.google.com",
                    listOf("store", "apps"),
                    DetailsDeepLink.Invalid
                ),
                arrayOf(
                    "example.com",
                    listOf("app", "abc123"),
                    DetailsDeepLink.Invalid
                ),

                // Invalid: null host
                arrayOf<Any?>(
                    null,
                    listOf("app", "abc123"),
                    DetailsDeepLink.Invalid
                ),

                // Invalid: wrong path structure - too few segments
                arrayOf(
                    "appteka.store",
                    listOf("app"),
                    DetailsDeepLink.Invalid
                ),
                arrayOf(
                    "appteka.store",
                    emptyList<String>(),
                    DetailsDeepLink.Invalid
                ),

                // Invalid: wrong path structure - too many segments
                arrayOf(
                    "appteka.store",
                    listOf("app", "abc", "extra"),
                    DetailsDeepLink.Invalid
                ),

                // Invalid: empty value
                arrayOf(
                    "appteka.store",
                    listOf("app", ""),
                    DetailsDeepLink.Invalid
                ),
                arrayOf(
                    "appteka.store",
                    listOf("app", "   "),
                    DetailsDeepLink.Invalid
                ),

                // Invalid: unsupported path type
                arrayOf(
                    "appteka.store",
                    listOf("unknown", "abc123"),
                    DetailsDeepLink.Invalid
                ),
                arrayOf(
                    "appteka.store",
                    listOf("profile", "123"),
                    DetailsDeepLink.Invalid
                ),

                // Invalid: null path
                arrayOf<Any?>(
                    "appteka.store",
                    null,
                    DetailsDeepLink.Invalid
                ),
            )
        }
    }
}
