package com.tomclaw.appsend.core.permissions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sanity-checks for the wire-code dictionaries.
 *
 * These objects are stable cross-tier contracts (must match Go's
 * `common/acl` and `service/capabilities_*.go`) — so the tests are
 * intentionally allergic to typos, duplicates, dot/underscore
 * confusions, and accidental whitespace.
 */
class AccessRuleTest {

    // --- AccessRule (rule codes) -------------------------------------

    @Test
    fun `AccessRule codes use snake_case and match the backend`() {
        // Same exact strings the Go server returns in
        // Capability.blockedBy. Renaming any of them silently breaks
        // hint-text resolution on the client.
        assertEquals("automoderation", AccessRule.AUTOMODERATION)
        assertEquals("feed_post_delete", AccessRule.FEED_POST_DELETE)
        assertEquals("list_downloaded", AccessRule.LIST_DOWNLOADED)
        assertEquals("final_moder_vote", AccessRule.FINAL_MODER_VOTE)
        assertEquals("app_unlink", AccessRule.APP_UNLINK)
        assertEquals("app_delete", AccessRule.APP_DELETE)
        assertEquals("app_unpublish", AccessRule.APP_UNPUBLISH)
        assertEquals("app_edit_meta", AccessRule.APP_EDIT_META)
        assertEquals("app_rating_delete", AccessRule.APP_RATING_DELETE)
        assertEquals("chat_topic_create", AccessRule.CHAT_TOPIC_CREATE)
        assertEquals("read_only_messages", AccessRule.READ_ONLY_MESSAGES)
        assertEquals("read_only_ratings", AccessRule.READ_ONLY_RATINGS)
        assertEquals("read_only_app_upload", AccessRule.READ_ONLY_APP_UPLOAD)
        assertEquals("read_only_feed_posts", AccessRule.READ_ONLY_FEED_POSTS)
    }

    @Test
    fun `AccessRule codes are unique`() {
        val codes = listOf(
            AccessRule.AUTOMODERATION,
            AccessRule.FEED_POST_DELETE,
            AccessRule.LIST_DOWNLOADED,
            AccessRule.FINAL_MODER_VOTE,
            AccessRule.APP_UNLINK,
            AccessRule.APP_DELETE,
            AccessRule.APP_UNPUBLISH,
            AccessRule.APP_EDIT_META,
            AccessRule.APP_RATING_DELETE,
            AccessRule.CHAT_TOPIC_CREATE,
            AccessRule.READ_ONLY_MESSAGES,
            AccessRule.READ_ONLY_RATINGS,
            AccessRule.READ_ONLY_APP_UPLOAD,
            AccessRule.READ_ONLY_FEED_POSTS,
        )
        assertEquals(
            "AccessRule has duplicate codes",
            codes.size, codes.toSet().size,
        )
    }

    @Test
    fun `AccessRule codes are non-empty and contain no whitespace`() {
        val all = listOf(
            AccessRule.AUTOMODERATION,
            AccessRule.FEED_POST_DELETE,
            AccessRule.LIST_DOWNLOADED,
            AccessRule.FINAL_MODER_VOTE,
            AccessRule.APP_UNLINK,
            AccessRule.APP_DELETE,
            AccessRule.APP_UNPUBLISH,
            AccessRule.APP_EDIT_META,
            AccessRule.APP_RATING_DELETE,
            AccessRule.CHAT_TOPIC_CREATE,
            AccessRule.READ_ONLY_MESSAGES,
            AccessRule.READ_ONLY_RATINGS,
            AccessRule.READ_ONLY_APP_UPLOAD,
            AccessRule.READ_ONLY_FEED_POSTS,
        )
        for (code in all) {
            assertTrue("rule code is empty", code.isNotEmpty())
            assertFalse("rule code contains whitespace: \"$code\"", code.any { it.isWhitespace() })
            // ACL rule codes use underscore (not dot) to distinguish
            // them from CapabilityAction identifiers (`app.unlink`).
            assertFalse("rule code unexpectedly contains a dot: \"$code\"", code.contains('.'))
        }
    }

    // --- CapabilityAction (action identifiers) -----------------------

    @Test
    fun `CapabilityAction identifiers use dotted-namespace and match the backend`() {
        // chat
        assertEquals("chat.message.send", CapabilityAction.CHAT_MESSAGE_SEND)
        assertEquals("chat.message.delete", CapabilityAction.CHAT_MESSAGE_DELETE)
        assertEquals("chat.message.report", CapabilityAction.CHAT_MESSAGE_REPORT)
        // app / ratings
        assertEquals("app.rate", CapabilityAction.APP_RATE)
        assertEquals("app.rating.delete", CapabilityAction.APP_RATING_DELETE)
        assertEquals("app.edit_meta", CapabilityAction.APP_EDIT_META)
        assertEquals("app.unpublish", CapabilityAction.APP_UNPUBLISH)
        assertEquals("app.unlink", CapabilityAction.APP_UNLINK)
        assertEquals("app.delete", CapabilityAction.APP_DELETE)
        // feed
        assertEquals("feed.post.delete", CapabilityAction.FEED_POST_DELETE)
        // global
        assertEquals("app.upload", CapabilityAction.APP_UPLOAD)
        assertEquals("app.upload.bypass_moderation", CapabilityAction.APP_UPLOAD_BYPASS_MODERATION)
        assertEquals("chat.topic.create", CapabilityAction.CHAT_TOPIC_CREATE)
        assertEquals("feed.post.create", CapabilityAction.FEED_POST_CREATE)
        assertEquals("moderation.enter", CapabilityAction.MODERATION_ENTER)
        assertEquals("moderation.final_vote", CapabilityAction.MODERATION_FINAL_VOTE)
    }

    @Test
    fun `CapabilityAction identifiers are unique and well-formed`() {
        val all = listOf(
            CapabilityAction.CHAT_MESSAGE_SEND,
            CapabilityAction.CHAT_MESSAGE_DELETE,
            CapabilityAction.CHAT_MESSAGE_REPORT,
            CapabilityAction.APP_RATE,
            CapabilityAction.APP_RATING_DELETE,
            CapabilityAction.APP_EDIT_META,
            CapabilityAction.APP_UNPUBLISH,
            CapabilityAction.APP_UNLINK,
            CapabilityAction.APP_DELETE,
            CapabilityAction.FEED_POST_DELETE,
            CapabilityAction.APP_UPLOAD,
            CapabilityAction.APP_UPLOAD_BYPASS_MODERATION,
            CapabilityAction.CHAT_TOPIC_CREATE,
            CapabilityAction.FEED_POST_CREATE,
            CapabilityAction.MODERATION_ENTER,
            CapabilityAction.MODERATION_FINAL_VOTE,
        )
        assertEquals(
            "duplicate CapabilityAction identifier",
            all.size, all.toSet().size,
        )
        for (id in all) {
            assertTrue("identifier is empty", id.isNotEmpty())
            assertFalse("identifier contains whitespace: \"$id\"", id.any { it.isWhitespace() })
            // Capability action namespace is dotted; underscore is
            // used inside a single segment ("edit_meta") only.
            assertTrue(
                "expected dotted action identifier: \"$id\"",
                id.contains('.'),
            )
        }
    }

    @Test
    fun `AccessRule and CapabilityAction namespaces do not overlap`() {
        // AccessRule uses snake_case, CapabilityAction uses dots —
        // overlap would mean somebody is mixing the two vocabularies.
        val rules = setOf(
            AccessRule.AUTOMODERATION,
            AccessRule.FEED_POST_DELETE,
            AccessRule.LIST_DOWNLOADED,
            AccessRule.FINAL_MODER_VOTE,
            AccessRule.APP_UNLINK,
            AccessRule.APP_DELETE,
            AccessRule.APP_UNPUBLISH,
            AccessRule.APP_EDIT_META,
            AccessRule.APP_RATING_DELETE,
            AccessRule.CHAT_TOPIC_CREATE,
            AccessRule.READ_ONLY_MESSAGES,
            AccessRule.READ_ONLY_RATINGS,
            AccessRule.READ_ONLY_APP_UPLOAD,
            AccessRule.READ_ONLY_FEED_POSTS,
        )
        val actions = setOf(
            CapabilityAction.CHAT_MESSAGE_SEND,
            CapabilityAction.APP_RATE,
            CapabilityAction.APP_EDIT_META,
            CapabilityAction.APP_UNLINK,
            CapabilityAction.APP_DELETE,
            CapabilityAction.APP_UNPUBLISH,
            CapabilityAction.MODERATION_ENTER,
        )
        assertTrue(
            "AccessRule and CapabilityAction codes overlap: ${rules.intersect(actions)}",
            rules.intersect(actions).isEmpty(),
        )
    }
}
