package com.tomclaw.appsend.core.permissions

import com.tomclaw.appsend.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The hint resolver decides which short text the UI shows when an
 * action is denied. The decision is one of three tiers:
 *   1. blockedBy → rule-specific copy (preferred, most specific).
 *   2. hintKey   → category copy (fallback).
 *   3. neither matches → null, caller renders generic "denied" copy.
 *
 * Tests use the pure [CapabilityHintResolver.lookupResId] helper so we
 * don't need an Android Resources instance — keeps the suite a plain
 * JVM test, no Robolectric.
 */
class CapabilityHintResolverTest {

    @Test
    fun `blockedBy is preferred over hintKey when both match`() {
        val cap = Capability(
            allowed = false,
            reason = CapabilityReason.RULE,
            blockedBy = AccessRule.READ_ONLY_MESSAGES,
            hintKey = "cap.reason.not_owner", // intentionally wrong category
        )
        // Rule-level wins because it's the more specific hint.
        assertEquals(
            R.string.permission_read_only_messages,
            CapabilityHintResolver.lookupResId(cap),
        )
    }

    @Test
    fun `hintKey is used when blockedBy is unknown`() {
        // Server might ship a brand-new ACL rule the client doesn't
        // know yet — we still resolve sensible copy via hintKey.
        val cap = Capability(
            allowed = false,
            reason = CapabilityReason.RULE,
            blockedBy = "future_acl_rule_we_dont_know_yet",
            hintKey = "cap.reason.not_owner",
        )
        assertEquals(R.string.permission_not_owner, CapabilityHintResolver.lookupResId(cap))
    }

    @Test
    fun `hintKey is used when blockedBy is missing`() {
        val cap = Capability(
            allowed = false,
            reason = CapabilityReason.AUTH,
            hintKey = "cap.reason.unauthorized",
        )
        assertEquals(R.string.permission_unauthorized, CapabilityHintResolver.lookupResId(cap))
    }

    @Test
    fun `unknown both — returns null so caller falls back to generic copy`() {
        val cap = Capability(
            allowed = false,
            reason = CapabilityReason.RULE,
            blockedBy = "nonexistent_rule",
            hintKey = "cap.reason.nonexistent",
        )
        assertNull(CapabilityHintResolver.lookupResId(cap))
    }

    @Test
    fun `null fields fall straight to null without crashing`() {
        val cap = Capability(allowed = false)
        assertNull(CapabilityHintResolver.lookupResId(cap))
    }

    @Test
    fun `empty strings are treated as missing`() {
        val cap = Capability(allowed = false, blockedBy = "", hintKey = "")
        assertNull(CapabilityHintResolver.lookupResId(cap))
    }

    @Test
    fun `every AccessRule constant has a registered string mapping`() {
        // Loud failure if someone adds a new AccessRule on the client
        // and forgets to wire its localised text. Catches the same
        // class of bug as the backend's TestRulesTableIsSane.
        val rules = listOf(
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
        for (rule in rules) {
            val cap = Capability(allowed = false, blockedBy = rule)
            val resId = CapabilityHintResolver.lookupResId(cap)
            assertNotNull("AccessRule.$rule has no string mapping", resId)
        }
    }

    @Test
    fun `every documented hintKey resolves`() {
        // Mirror of the hintKeyTextResources map — catches typos and
        // dropped entries.
        val keys = listOf(
            "cap.reason.unauthorized",
            "cap.reason.not_owner",
            "cap.reason.banned",
            "cap.reason.role_too_low",
            "cap.reason.read_only_messages",
            "cap.reason.read_only_ratings",
            "cap.reason.feed_post_delete",
            "cap.reason.automoderation",
            "cap.reason.list_downloaded",
            "cap.reason.final_moder_vote",
            "cap.reason.app_unlink",
            "cap.reason.app_delete",
            "cap.reason.app_unpublish",
            "cap.reason.app_edit_meta",
            "cap.reason.app_protected",
            "cap.reason.app_already_unpublished",
            "cap.reason.app_rating_delete",
            "cap.reason.chat_topic_create",
            "cap.reason.read_only_app_upload",
            "cap.reason.read_only_feed_posts",
        )
        for (key in keys) {
            val cap = Capability(allowed = false, hintKey = key)
            val resId = CapabilityHintResolver.lookupResId(cap)
            assertNotNull("hintKey \"$key\" has no string mapping", resId)
        }
    }

    @Test
    fun `unknown blockedBy with empty hintKey falls through cleanly`() {
        val cap = Capability(
            allowed = false,
            blockedBy = "future_rule",
            hintKey = "",
        )
        assertNull(CapabilityHintResolver.lookupResId(cap))
    }
}
