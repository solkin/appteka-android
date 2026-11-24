package com.tomclaw.appsend.screen.ratings

import com.tomclaw.appsend.core.permissions.AccessRule
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.core.permissions.CapabilityReason
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.dto.UserMark
import com.tomclaw.appsend.screen.details.api.RatingEntity
import com.tomclaw.appsend.user.api.UserBrief
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RatingConverterTest {

    private val converter = RatingConverterImpl()

    @Test
    fun `server-allowed capability exposes menu`() {
        val item = converter.convert(
            entity = rating(
                userId = 42,
                capabilities = mapOf(
                    CapabilityAction.APP_RATING_DELETE to Capability(allowed = true),
                ),
            ),
            brief = brief(userId = 7, role = 0),
            currentVersionCode = null,
        )
        assertTrue(item.showRatingMenu)
    }

    @Test
    fun `server-denied capability wins over legacy ownership check`() {
        val item = converter.convert(
            entity = rating(
                userId = 42,
                capabilities = mapOf(
                    CapabilityAction.APP_RATING_DELETE to Capability(
                        allowed = false,
                        reason = CapabilityReason.OWNERSHIP,
                        blockedBy = null,
                    ),
                ),
            ),
            // Legacy heuristic would say "yes, you are the author",
            // but the server-resolved capability explicitly disagrees.
            brief = brief(userId = 42, role = 0),
            currentVersionCode = null,
        )
        assertFalse(item.showRatingMenu)
    }

    @Test
    fun `legacy heuristic applies when capabilities are missing`() {
        val admin = brief(userId = 7, role = 200)
        val itemForAdmin = converter.convert(
            entity = rating(userId = 42, capabilities = null),
            brief = admin,
            currentVersionCode = null,
        )
        assertTrue("admin should see the menu via legacy check", itemForAdmin.showRatingMenu)

        val otherUser = brief(userId = 7, role = 0)
        val itemForOther = converter.convert(
            entity = rating(userId = 42, capabilities = null),
            brief = otherUser,
            currentVersionCode = null,
        )
        assertFalse(
            "non-admin unrelated user should not see the menu",
            itemForOther.showRatingMenu,
        )

        val owner = brief(userId = 42, role = 0)
        val itemForOwner = converter.convert(
            entity = rating(userId = 42, capabilities = null),
            brief = owner,
            currentVersionCode = null,
        )
        assertTrue("rating author should see the menu via legacy check", itemForOwner.showRatingMenu)
    }

    @Test
    fun `legacy heuristic hides the menu for anonymous viewer`() {
        val item = converter.convert(
            entity = rating(userId = 42, capabilities = null),
            brief = null,
            currentVersionCode = null,
        )
        assertFalse(item.showRatingMenu)
    }

    @Test
    fun `blocked_by rule name is preserved in capability even if policy hides the menu`() {
        // This test guards against accidental null-map regressions: the
        // converter must forward blocked_by back through the Capability.
        val readOnly = Capability(
            allowed = false,
            reason = CapabilityReason.RULE,
            blockedBy = AccessRule.READ_ONLY_RATINGS,
        )
        val caps = mapOf(CapabilityAction.APP_RATING_DELETE to readOnly)
        val item = converter.convert(
            entity = rating(userId = 42, capabilities = caps),
            brief = brief(userId = 42, role = 0),
            currentVersionCode = null,
        )
        assertFalse(item.showRatingMenu)
    }

    @Test
    fun `review version is forwarded to rating item`() {
        val withVersion = converter.convert(
            entity = rating(userId = 42, capabilities = null, verName = "2.4.1"),
            brief = null,
            currentVersionCode = null,
        )
        assertEquals("2.4.1", withVersion.versionName)

        val withoutVersion = converter.convert(
            entity = rating(userId = 42, capabilities = null, verName = null),
            brief = null,
            currentVersionCode = null,
        )
        assertNull(withoutVersion.versionName)
    }

    @Test
    fun `review version is hidden only when it matches the current build`() {
        val sameBuild = converter.convert(
            entity = rating(userId = 42, capabilities = null, verName = "2.4.1", verCode = 241),
            brief = null,
            currentVersionCode = 241,
        )
        assertNull(sameBuild.versionName)

        val otherBuild = converter.convert(
            entity = rating(userId = 42, capabilities = null, verName = "2.3.0", verCode = 230),
            brief = null,
            currentVersionCode = 241,
        )
        assertEquals("2.3.0", otherBuild.versionName)

        // Without a version code there is nothing to compare, so the name stays.
        val unknownCode = converter.convert(
            entity = rating(userId = 42, capabilities = null, verName = "2.4.1", verCode = null),
            brief = null,
            currentVersionCode = 241,
        )
        assertEquals("2.4.1", unknownCode.versionName)
    }

    // --- Factories ---------------------------------------------------

    private fun rating(
        userId: Int,
        capabilities: Map<String, Capability>?,
        verName: String? = null,
        verCode: Int? = null,
    ) = RatingEntity(
        rateId = 1,
        score = 5,
        text = "Nice",
        time = 0,
        user = UserMark(
            id = userId,
            name = "Reviewer",
            icon = UserIcon(icon = "", label = emptyMap(), color = "#000"),
        ),
        verName = verName,
        verCode = verCode,
        capabilities = capabilities,
    )

    private fun brief(userId: Int, role: Int) = UserBrief(
        id = userId,
        icon = UserIcon(icon = "", label = emptyMap(), color = "#000"),
        joinTime = 0,
        lastSeen = 0,
        role = role,
        name = "User",
        isRegistered = true,
        isVerified = false,
        url = null,
    )
}
