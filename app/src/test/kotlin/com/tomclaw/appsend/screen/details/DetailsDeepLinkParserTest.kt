package com.tomclaw.appsend.screen.details

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DetailsDeepLinkParserTest(
    private val host: String?,
    private val pathSegments: List<String>?,
    private val fragment: String?,
    private val expected: DetailsDeepLink
) {

    @Test
    fun parseDeepLinkTest() {
        val result = DetailsDeepLinkParserImpl.parse(host, pathSegments, fragment)
        assertEquals(expected, result)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "host={0}, path={1}, fragment={2} -> {3}")
        fun data(): Collection<Array<Any?>> {
            return listOf(
                // Valid app URLs
                arrayOf(
                    "appteka.store",
                    listOf("app", "abc123"),
                    null,
                    DetailsDeepLink.ByAppId("abc123")
                ),
                arrayOf(
                    "appteka.store",
                    listOf("apps", "abc123"),
                    null,
                    DetailsDeepLink.ByAppId("abc123")
                ),
                arrayOf(
                    "appteka.store",
                    listOf("app", "12345"),
                    null,
                    DetailsDeepLink.ByAppId("12345")
                ),

                // Valid app URL with #review fragment
                arrayOf(
                    "appteka.store",
                    listOf("app", "abc123"),
                    "review",
                    DetailsDeepLink.ByAppId("abc123", openReview = true)
                ),
                arrayOf(
                    "appteka.store",
                    listOf("apps", "abc123"),
                    "review",
                    DetailsDeepLink.ByAppId("abc123", openReview = true)
                ),

                // App URL with non-review fragment
                arrayOf(
                    "appteka.store",
                    listOf("app", "abc123"),
                    "other",
                    DetailsDeepLink.ByAppId("abc123", openReview = false)
                ),

                // Package URL ignores fragment
                arrayOf(
                    "appteka.store",
                    listOf("package", "com.example.app"),
                    "review",
                    DetailsDeepLink.ByPackageName("com.example.app")
                ),

                // Valid package URLs
                arrayOf(
                    "appteka.store",
                    listOf("package", "com.example.app"),
                    null,
                    DetailsDeepLink.ByPackageName("com.example.app")
                ),

                // Invalid: wrong host
                arrayOf(
                    "appsend.store",
                    listOf("app", "abc123"),
                    null,
                    DetailsDeepLink.Invalid
                ),
                arrayOf(
                    "play.google.com",
                    listOf("store", "apps"),
                    null,
                    DetailsDeepLink.Invalid
                ),
                arrayOf(
                    "example.com",
                    listOf("app", "abc123"),
                    null,
                    DetailsDeepLink.Invalid
                ),

                // Invalid: null host
                arrayOf<Any?>(
                    null,
                    listOf("app", "abc123"),
                    null,
                    DetailsDeepLink.Invalid
                ),

                // Invalid: wrong path structure - too few segments
                arrayOf(
                    "appteka.store",
                    listOf("app"),
                    null,
                    DetailsDeepLink.Invalid
                ),
                arrayOf(
                    "appteka.store",
                    emptyList<String>(),
                    null,
                    DetailsDeepLink.Invalid
                ),

                // Invalid: wrong path structure - too many segments
                arrayOf(
                    "appteka.store",
                    listOf("app", "abc", "extra"),
                    null,
                    DetailsDeepLink.Invalid
                ),

                // Invalid: empty value
                arrayOf(
                    "appteka.store",
                    listOf("app", ""),
                    null,
                    DetailsDeepLink.Invalid
                ),
                arrayOf(
                    "appteka.store",
                    listOf("app", "   "),
                    null,
                    DetailsDeepLink.Invalid
                ),

                // Invalid: unsupported path type
                arrayOf(
                    "appteka.store",
                    listOf("unknown", "abc123"),
                    null,
                    DetailsDeepLink.Invalid
                ),
                arrayOf(
                    "appteka.store",
                    listOf("profile", "123"),
                    null,
                    DetailsDeepLink.Invalid
                ),

                // Invalid: null path
                arrayOf<Any?>(
                    "appteka.store",
                    null,
                    null,
                    DetailsDeepLink.Invalid
                ),
            )
        }
    }
}
