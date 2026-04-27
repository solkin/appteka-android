package com.tomclaw.appsend.screen.profile

import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.home.api.ModerationData
import com.tomclaw.appsend.screen.profile.adapter.moderation.ModerationItem
import com.tomclaw.appsend.screen.profile.api.Profile
import com.tomclaw.appsend.util.adapter.Item
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the moderation-entry visibility logic: the server-resolved
 * "moderation.enter" capability must override the legacy
 * `ModerationData.moderator` flag, and the legacy flag must keep
 * working as a fallback when the capability snapshot is missing.
 */
class ProfileConverterModerationTest {

    private val converter = ProfileConverterImpl()

    @Test
    fun `capability allowed shows moderation item when user is self`() {
        val items = converter.convertProfile(
            profile = profile(),
            uploads = null,
            moderation = ModerationData(moderator = true, count = 5),
            isSelf = true,
            userCapabilities = mapOf(
                CapabilityAction.MODERATION_ENTER to Capability(allowed = true),
            ),
        )
        assertTrue(items.hasModerationItem())
    }

    @Test
    fun `capability denied hides moderation even when legacy moderator flag is true`() {
        val items = converter.convertProfile(
            profile = profile(),
            uploads = null,
            moderation = ModerationData(moderator = true, count = 5),
            isSelf = true,
            userCapabilities = mapOf(
                CapabilityAction.MODERATION_ENTER to Capability(allowed = false),
            ),
        )
        assertFalse(items.hasModerationItem())
    }

    @Test
    fun `legacy fallback shows moderation when capabilities are missing`() {
        val items = converter.convertProfile(
            profile = profile(),
            uploads = null,
            moderation = ModerationData(moderator = true, count = 3),
            isSelf = true,
            userCapabilities = null,
        )
        assertTrue(items.hasModerationItem())
    }

    @Test
    fun `legacy fallback hides moderation for non-moderator when capabilities are missing`() {
        val items = converter.convertProfile(
            profile = profile(),
            uploads = null,
            moderation = ModerationData(moderator = false, count = 0),
            isSelf = true,
            userCapabilities = null,
        )
        assertFalse(items.hasModerationItem())
    }

    @Test
    fun `moderation item never shown for foreign profile`() {
        val items = converter.convertProfile(
            profile = profile(),
            uploads = null,
            moderation = ModerationData(moderator = true, count = 5),
            isSelf = false,
            userCapabilities = mapOf(
                CapabilityAction.MODERATION_ENTER to Capability(allowed = true),
            ),
        )
        assertFalse(items.hasModerationItem())
    }

    private fun List<Item>.hasModerationItem() = any { it is ModerationItem }

    private fun profile() = Profile(
        userId = 1,
        userIcon = UserIcon(icon = "", label = emptyMap(), color = "#000"),
    )
}
