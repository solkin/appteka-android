package com.tomclaw.appsend.screen.profile

import android.net.Uri

sealed class ProfileDeepLink {
    data class ByUserId(val userId: Int) : ProfileDeepLink()
    data object Invalid : ProfileDeepLink()
}

interface ProfileDeepLinkParser {
    fun parse(uri: Uri?): ProfileDeepLink
}

class ProfileDeepLinkParserImpl : ProfileDeepLinkParser {

    override fun parse(uri: Uri?): ProfileDeepLink {
        if (uri == null) return ProfileDeepLink.Invalid
        return parse(
            host = uri.host,
            pathSegments = uri.pathSegments
        )
    }

    companion object {
        private const val HOST_APPTEKA = "appteka.store"
        private const val PATH_PROFILE = "profile"
        private const val PATH_USER = "user"

        fun parse(host: String?, pathSegments: List<String>?): ProfileDeepLink {
            if (host != HOST_APPTEKA) return ProfileDeepLink.Invalid

            if (pathSegments == null || pathSegments.size != 2) return ProfileDeepLink.Invalid

            val type = pathSegments[0]
            val value = pathSegments[1]

            if (type != PATH_PROFILE && type != PATH_USER) return ProfileDeepLink.Invalid

            val userId = value.toIntOrNull()
            if (userId == null || userId <= 0) return ProfileDeepLink.Invalid

            return ProfileDeepLink.ByUserId(userId)
        }
    }
}
