package com.tomclaw.appsend.core.permissions

/**
 * Stable codes for user-facing ACL rules. Must match
 * `auth.UserRules()` on the backend. This object is the single
 * source of truth for every place on the client that needs to map
 * [Capability.blockedBy] back to a human-readable hint or icon.
 *
 * NB: the server transports rules as string codes (not numeric IDs),
 * so the client never has to know the underlying integer values — it is
 * enough to know the code.
 */
object AccessRule {
    const val AUTOMODERATION = "automoderation"
    const val FEED_POST_DELETE = "feed_post_delete"
    const val LIST_DOWNLOADED = "list_downloaded"
    const val FINAL_MODER_VOTE = "final_moder_vote"
    const val APP_UNLINK = "app_unlink"
    const val APP_DELETE = "app_delete"
    const val APP_UNPUBLISH = "app_unpublish"
    const val APP_EDIT_META = "app_edit_meta"
    const val APP_RATING_DELETE = "app_rating_delete"
    const val CHAT_TOPIC_CREATE = "chat_topic_create"
    const val READ_ONLY_MESSAGES = "read_only_messages"
    const val READ_ONLY_RATINGS = "read_only_ratings"
    const val READ_ONLY_APP_UPLOAD = "read_only_app_upload"
    const val READ_ONLY_FEED_POSTS = "read_only_feed_posts"
}

/**
 * Stable identifiers for the user-facing actions exposed via
 * Capability maps. Must match the `Action*` constants in the backend
 * `capabilities_*.go` files.
 *
 * Resource-scoped actions arrive embedded in the relevant resource
 * payload (chat topic, message, rating, post). Global actions (those
 * named without a specific resource context, e.g. [APP_UPLOAD]) come
 * via `/api/1/user/capabilities` and live in
 * [UserCapabilitiesProvider].
 */
object CapabilityAction {
    // Chat — resource-scoped
    const val CHAT_MESSAGE_SEND = "chat.message.send"
    const val CHAT_MESSAGE_DELETE = "chat.message.delete"
    const val CHAT_MESSAGE_REPORT = "chat.message.report"

    // App / ratings — resource-scoped
    const val APP_RATE = "app.rate"
    const val APP_RATING_DELETE = "app.rating.delete"
    const val APP_EDIT_META = "app.edit_meta"
    const val APP_UNPUBLISH = "app.unpublish"
    const val APP_UNLINK = "app.unlink"
    const val APP_DELETE = "app.delete"

    // Feed — resource-scoped
    const val FEED_POST_DELETE = "feed.post.delete"

    // Global (user-scoped). Drive top-level affordances — FABs,
    // navigation entries, informational copy on shared screens.
    const val APP_UPLOAD = "app.upload"
    const val APP_UPLOAD_BYPASS_MODERATION = "app.upload.bypass_moderation"
    const val CHAT_TOPIC_CREATE = "chat.topic.create"
    const val FEED_POST_CREATE = "feed.post.create"
    const val MODERATION_ENTER = "moderation.enter"
    const val MODERATION_FINAL_VOTE = "moderation.final_vote"
}
