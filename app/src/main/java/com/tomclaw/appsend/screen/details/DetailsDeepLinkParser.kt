package com.tomclaw.appsend.screen.details

import android.net.Uri

sealed class DetailsDeepLink {
    data class ByAppId(val appId: String, val openReview: Boolean = false) : DetailsDeepLink()
    data class ByPackageName(val packageName: String) : DetailsDeepLink()
    data object Invalid : DetailsDeepLink()
}

interface DetailsDeepLinkParser {
    fun parse(uri: Uri?): DetailsDeepLink
}

class DetailsDeepLinkParserImpl : DetailsDeepLinkParser {

    override fun parse(uri: Uri?): DetailsDeepLink {
        if (uri == null) return DetailsDeepLink.Invalid
        return parse(
            host = uri.host,
            pathSegments = uri.pathSegments,
            fragment = uri.fragment
        )
    }

    companion object {
        private const val HOST_APPTEKA = "appteka.store"
        private const val PATH_APP = "app"
        private const val PATH_APPS = "apps"
        private const val PATH_PACKAGE = "package"
        private const val FRAGMENT_REVIEW = "review"

        fun parse(
            host: String?,
            pathSegments: List<String>?,
            fragment: String? = null
        ): DetailsDeepLink {
            if (host != HOST_APPTEKA) return DetailsDeepLink.Invalid

            if (pathSegments == null || pathSegments.size != 2) return DetailsDeepLink.Invalid

            val type = pathSegments[0]
            val value = pathSegments[1]

            if (value.isBlank()) return DetailsDeepLink.Invalid

            return when (type) {
                PATH_APP, PATH_APPS -> DetailsDeepLink.ByAppId(
                    appId = value,
                    openReview = fragment == FRAGMENT_REVIEW
                )
                PATH_PACKAGE -> DetailsDeepLink.ByPackageName(value)
                else -> DetailsDeepLink.Invalid
            }
        }
    }
}
