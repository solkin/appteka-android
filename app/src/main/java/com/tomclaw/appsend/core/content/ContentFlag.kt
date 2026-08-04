package com.tomclaw.appsend.core.content

import androidx.annotation.StringRes
import com.tomclaw.appsend.R

/**
 * Content a listing may carry that a viewer can choose not to see.
 *
 * Codes are the backend's stable vocabulary (see the server's
 * `common/content` registry) and travel over the wire in both
 * directions: the profile carries the set the viewer hides, app
 * metadata carries what a particular app was found to contain. The
 * client owns nothing but the localisation, same arrangement as
 * [com.tomclaw.appsend.core.permissions.CapabilityHintResolver].
 *
 * Declaration order is the order these are offered in settings and
 * rendered as chips, and matches the server's registry so the two
 * screens read alike.
 */
enum class ContentFlag(
    val code: String,
    @get:StringRes val titleRes: Int,
    @get:StringRes val summaryRes: Int,
) {

    ADULT("adult", R.string.content_flag_adult, R.string.content_flag_adult_summary),
    GAMBLING("gambling", R.string.content_flag_gambling, R.string.content_flag_gambling_summary),
    VIOLENCE("violence", R.string.content_flag_violence, R.string.content_flag_violence_summary),
    SUBSTANCES(
        "substances",
        R.string.content_flag_substances,
        R.string.content_flag_substances_summary,
    ),
    DATING("dating", R.string.content_flag_dating, R.string.content_flag_dating_summary);

    companion object {

        fun fromCode(code: String): ContentFlag? = entries.find { it.code == code }

        /**
         * Reads a set of codes off the wire. Anything unknown is dropped
         * rather than rejected: a newer server may name a flag this
         * build has never heard of, and that must not cost us the ones
         * it does understand.
         */
        fun fromCodes(codes: List<String>?): Set<ContentFlag> =
            codes.orEmpty().mapNotNull { fromCode(it) }.toSet()

        /** Wire form of a selection, in registry order. */
        fun codesOf(flags: Set<ContentFlag>): List<String> =
            entries.filter { it in flags }.map { it.code }

    }

}
