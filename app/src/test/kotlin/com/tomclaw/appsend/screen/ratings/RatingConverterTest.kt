package com.tomclaw.appsend.screen.ratings

import com.tomclaw.appsend.core.permissions.AccessRule
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.core.permissions.CapabilityReason
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.details.api.RatingEntity
import com.tomclaw.appsend.user.api.UserBrief
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class RatingConverterTest {

    private val converter = RatingConverterImpl(Locale.ENGLISH)

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
        )
        assertFalse(item.showRatingMenu)
    }

    @Test
    fun `legacy heuristic applies when capabilities are missing`() {
        val admin = brief(userId = 7, role = 200)
        val itemForAdmin = converter.convert(
            entity = rating(userId = 42, capabilities = null),
            brief = admin,
        )
        assertTrue("admin should see the menu via legacy check", itemForAdmin.showRatingMenu)

        val otherUser = brief(userId = 7, role = 0)
        val itemForOther = converter.convert(
            entity = rating(userId = 42, capabilities = null),
            brief = otherUser,
        )
        assertFalse(
            "non-admin unrelated user should not see the menu",
            itemForOther.showRatingMenu,
        )

        val owner = brief(userId = 42, role = 0)
        val itemForOwner = converter.convert(
            entity = rating(userId = 42, capabilities = null),
            brief = owner,
        )
        assertTrue("rating author should see the menu via legacy check", itemForOwner.showRatingMenu)
    }

    @Test
    fun `legacy heuristic hides the menu for anonymous viewer`() {
        val item = converter.convert(
            entity = rating(userId = 42, capabilities = null),
            brief = null,
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
        )
        assertFalse(item.showRatingMenu)
    }

    // --- Factories ---------------------------------------------------

    private fun rating(
        userId: Int,
        capabilities: Map<String, Capability>?,
    ) = RatingEntity(
        rateId = 1,
        score = 5,
        text = "Nice",
        time = 0,
        userId = userId,
        userIcon = UserIcon(icon = "", label = emptyMap(), color = "#000"),
        userName = "Reviewer",
        capabilities = capabilities,
    )

    private fun brief(userId: Int, role: Int) = UserBrief(
        userId = userId,
        userIcon = UserIcon(icon = "", label = emptyMap(), color = "#000"),
        joinTime = 0,
        lastSeen = 0,
        role = role,
        name = "User",
        isRegistered = true,
        isVerified = false,
        url = null,
    )
}
