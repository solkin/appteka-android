package com.tomclaw.appsend.core.permissions

import android.content.res.Resources
import androidx.annotation.StringRes
import com.tomclaw.appsend.R

/**
 * Maps [Capability] into the short user-facing text that explains why an
 * action is blocked. Hint keys are stable and come from the backend; the
 * client owns the localisation and the actual string resources.
 *
 * Two lookup paths:
 * 1. By [Capability.blockedBy] — the ACL rule code — so the same text is
 *    reused everywhere the same rule applies.
 * 2. By [Capability.hintKey] — generic reasons like `not_owner` or
 *    `unauthorized` that are not tied to a specific rule.
 *
 * `blockedBy` always wins over `hintKey` when both are set: an explicit
 * rule code is more specific than a category-level hint.
 */
class CapabilityHintResolver(private val resources: Resources) {

    fun resolveText(capability: Capability): String {
        val resId = resolveTextResId(capability) ?: R.string.permission_generic_denied
        return resources.getString(resId)
    }

    @StringRes
    fun resolveTextResId(capability: Capability): Int? = lookupResId(capability)

    companion object {
        /**
         * Pure resource-id lookup, separated from the instance API so it
         * can be unit-tested without an Android [Resources] context.
         * Returns `null` when neither `blockedBy` nor `hintKey` matches
         * a known mapping — the caller is responsible for falling back
         * to a generic string.
         */
        @StringRes
        @JvmStatic
        fun lookupResId(capability: Capability): Int? {
            capability.blockedBy?.takeIf { it.isNotEmpty() }?.let { code ->
                ruleTextResources[code]?.let { return it }
            }
            capability.hintKey?.takeIf { it.isNotEmpty() }?.let { key ->
                hintKeyTextResources[key]?.let { return it }
            }
            return null
        }

        private val ruleTextResources: Map<String, Int> = mapOf(
            AccessRule.READ_ONLY_MESSAGES to R.string.permission_read_only_messages,
            AccessRule.READ_ONLY_RATINGS to R.string.permission_read_only_ratings,
            AccessRule.FEED_POST_DELETE to R.string.permission_feed_post_delete,
            AccessRule.AUTOMODERATION to R.string.permission_automoderation,
            AccessRule.LIST_DOWNLOADED to R.string.permission_list_downloaded,
            AccessRule.FINAL_MODER_VOTE to R.string.permission_final_moder_vote,
            AccessRule.APP_UNLINK to R.string.permission_app_unlink,
            AccessRule.APP_DELETE to R.string.permission_app_delete,
            AccessRule.APP_UNPUBLISH to R.string.permission_app_unpublish,
            AccessRule.APP_EDIT_META to R.string.permission_app_edit_meta,
            AccessRule.APP_RATING_DELETE to R.string.permission_app_rating_delete,
            AccessRule.CHAT_TOPIC_CREATE to R.string.permission_chat_topic_create,
            AccessRule.READ_ONLY_APP_UPLOAD to R.string.permission_read_only_app_upload,
            AccessRule.READ_ONLY_FEED_POSTS to R.string.permission_read_only_feed_posts,
        )

        private val hintKeyTextResources: Map<String, Int> = mapOf(
            "cap.reason.unauthorized" to R.string.permission_unauthorized,
            "cap.reason.not_owner" to R.string.permission_not_owner,
            "cap.reason.banned" to R.string.permission_banned,
            "cap.reason.role_too_low" to R.string.permission_role_too_low,
            "cap.reason.read_only_messages" to R.string.permission_read_only_messages,
            "cap.reason.read_only_ratings" to R.string.permission_read_only_ratings,
            "cap.reason.feed_post_delete" to R.string.permission_feed_post_delete,
            "cap.reason.automoderation" to R.string.permission_automoderation,
            "cap.reason.list_downloaded" to R.string.permission_list_downloaded,
            "cap.reason.final_moder_vote" to R.string.permission_final_moder_vote,
            "cap.reason.app_unlink" to R.string.permission_app_unlink,
            "cap.reason.app_delete" to R.string.permission_app_delete,
            "cap.reason.app_unpublish" to R.string.permission_app_unpublish,
            "cap.reason.app_edit_meta" to R.string.permission_app_edit_meta,
            "cap.reason.app_protected" to R.string.permission_app_protected,
            "cap.reason.app_already_unpublished" to R.string.permission_app_already_unpublished,
            "cap.reason.app_rating_delete" to R.string.permission_app_rating_delete,
            "cap.reason.chat_topic_create" to R.string.permission_chat_topic_create,
            "cap.reason.read_only_app_upload" to R.string.permission_read_only_app_upload,
            "cap.reason.read_only_feed_posts" to R.string.permission_read_only_feed_posts,
        )
    }
}
