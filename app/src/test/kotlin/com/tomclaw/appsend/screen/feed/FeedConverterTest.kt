package com.tomclaw.appsend.screen.feed

import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.feed.api.ACTION_DELETE
import com.tomclaw.appsend.screen.feed.api.PostEntity
import com.tomclaw.appsend.screen.feed.api.TYPE_TEXT
import com.tomclaw.appsend.screen.feed.api.TextPayload
import com.tomclaw.appsend.user.api.UserBrief
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedConverterTest {

    private val converter = FeedConverterImpl()

    @Test
    fun `capabilities override legacy actions`() {
        // Legacy actions say "delete" is allowed, but the capability
        // says it isn't — the capability must win.
        val item = converter.convert(
            post = post(
                legacyActions = listOf(ACTION_DELETE),
                capabilities = mapOf(
                    CapabilityAction.FEED_POST_DELETE to Capability(allowed = false),
                ),
            ),
        )
        assertTrue(item!!.actions.isNullOrEmpty())
    }

    @Test
    fun `allowed capability produces delete action`() {
        val item = converter.convert(
            post = post(
                legacyActions = null,
                capabilities = mapOf(
                    CapabilityAction.FEED_POST_DELETE to Capability(allowed = true),
                ),
            ),
        )
        assertEquals(listOf(ACTION_DELETE), item!!.actions)
    }

    @Test
    fun `falls back to legacy actions when no capabilities`() {
        val item = converter.convert(
            post = post(legacyActions = listOf(ACTION_DELETE), capabilities = null),
        )
        assertEquals(listOf(ACTION_DELETE), item!!.actions)
    }

    @Test
    fun `no actions when neither capabilities nor legacy actions present`() {
        val item = converter.convert(
            post = post(legacyActions = null, capabilities = null),
        )
        assertNull(item!!.actions)
    }

    private fun post(
        legacyActions: List<String>?,
        capabilities: Map<String, Capability>?,
    ) = PostEntity(
        postId = 1,
        time = 0,
        type = TYPE_TEXT,
        payload = TextPayload(screenshots = emptyList(), text = "hi"),
        reacts = null,
        user = UserBrief(
            id = 1,
            icon = UserIcon(icon = "", label = emptyMap(), color = "#000"),
            joinTime = 0,
            lastSeen = 0,
            role = 0,
            name = "author",
            isRegistered = true,
            isVerified = false,
            url = null,
        ),
        actions = legacyActions,
        capabilities = capabilities,
    )
}
