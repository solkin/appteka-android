package com.tomclaw.appsend.core.permissions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coverage of [CapabilityPolicy] — the single decision point that
 * every UI surface uses to translate a capability snapshot into a
 * yes/no/unknown answer. The class is tiny on purpose; the tests
 * pin down every branch including the boundary cases that have
 * already bitten us once (forgetting [CapabilityResult.Unknown]
 * fallback, mistaking `allowed=false` for `Unknown`, etc.).
 */
class CapabilityPolicyTest {

    // --- check() -----------------------------------------------------

    @Test
    fun `check returns Allowed for an allowed capability`() {
        val caps = mapOf(CapabilityAction.CHAT_MESSAGE_SEND to Capability(allowed = true))
        val result = CapabilityPolicy.check(CapabilityAction.CHAT_MESSAGE_SEND, caps)
        assertSame(CapabilityResult.Allowed, result)
    }

    @Test
    fun `check returns Denied with the original Capability instance`() {
        val denied = Capability(
            allowed = false,
            reason = CapabilityReason.RULE,
            blockedBy = AccessRule.READ_ONLY_MESSAGES,
            hintKey = "cap.reason.read_only_messages",
        )
        val caps = mapOf(CapabilityAction.CHAT_MESSAGE_SEND to denied)

        val result = CapabilityPolicy.check(CapabilityAction.CHAT_MESSAGE_SEND, caps)

        assertTrue(result is CapabilityResult.Denied)
        // Forwarding the *same* Capability matters: callers like the
        // UI hint resolver inspect blockedBy / hintKey directly to
        // decide which copy to render. Drop a field on the way and
        // the tooltip silently goes generic.
        assertEquals(denied, (result as CapabilityResult.Denied).capability)
    }

    @Test
    fun `check returns Unknown when the action is missing from a non-empty map`() {
        val caps = mapOf(CapabilityAction.APP_RATE to Capability(allowed = true))
        val result = CapabilityPolicy.check(CapabilityAction.CHAT_MESSAGE_SEND, caps)
        assertSame(CapabilityResult.Unknown, result)
    }

    @Test
    fun `check returns Unknown for an empty capability map`() {
        val result = CapabilityPolicy.check(CapabilityAction.CHAT_MESSAGE_SEND, emptyMap())
        assertSame(CapabilityResult.Unknown, result)
    }

    @Test
    fun `check returns Unknown for a null capability map`() {
        val result = CapabilityPolicy.check(CapabilityAction.CHAT_MESSAGE_SEND, null)
        assertSame(CapabilityResult.Unknown, result)
    }

    @Test
    fun `check picks the action it is asked about — not a neighbour`() {
        // Guards against accidental "first entry wins" bugs in iteration
        // and against case-insensitive lookups.
        val caps = mapOf(
            CapabilityAction.CHAT_MESSAGE_SEND to Capability(allowed = false),
            CapabilityAction.CHAT_MESSAGE_REPORT to Capability(allowed = true),
        )
        val send = CapabilityPolicy.check(CapabilityAction.CHAT_MESSAGE_SEND, caps)
        val report = CapabilityPolicy.check(CapabilityAction.CHAT_MESSAGE_REPORT, caps)

        assertTrue("send must be denied", send is CapabilityResult.Denied)
        assertSame("report must be allowed", CapabilityResult.Allowed, report)
    }

    @Test
    fun `check is case-sensitive on the action key`() {
        val caps = mapOf(CapabilityAction.CHAT_MESSAGE_SEND to Capability(allowed = true))
        val result = CapabilityPolicy.check("CHAT.MESSAGE.SEND", caps)
        assertSame(CapabilityResult.Unknown, result)
    }

    @Test
    fun `Denied carries even partially populated reasons`() {
        // Server might omit blockedBy when the deny is reason=auth/role
        // — the wrapper must still arrive intact at the hint resolver.
        val denied = Capability(allowed = false, reason = CapabilityReason.AUTH)
        val caps = mapOf(CapabilityAction.APP_UPLOAD to denied)

        val result = CapabilityPolicy.check(CapabilityAction.APP_UPLOAD, caps)

        assertTrue(result is CapabilityResult.Denied)
        val cap = (result as CapabilityResult.Denied).capability
        assertEquals(CapabilityReason.AUTH, cap.reason)
        assertEquals(null, cap.blockedBy)
        assertEquals(null, cap.hintKey)
    }

    // --- isAllowed() -------------------------------------------------

    @Test
    fun `isAllowed returns true on Allowed regardless of fallback`() {
        val caps = mapOf(CapabilityAction.APP_RATE to Capability(allowed = true))
        assertTrue(CapabilityPolicy.isAllowed(CapabilityAction.APP_RATE, caps, allowOnUnknown = false))
        assertTrue(CapabilityPolicy.isAllowed(CapabilityAction.APP_RATE, caps, allowOnUnknown = true))
    }

    @Test
    fun `isAllowed returns false on Denied regardless of fallback`() {
        val caps = mapOf(CapabilityAction.APP_RATE to Capability(allowed = false))
        assertFalse(CapabilityPolicy.isAllowed(CapabilityAction.APP_RATE, caps, allowOnUnknown = true))
        assertFalse(CapabilityPolicy.isAllowed(CapabilityAction.APP_RATE, caps, allowOnUnknown = false))
    }

    @Test
    fun `isAllowed honours allowOnUnknown when capability is missing`() {
        assertTrue(
            "Unknown + allowOnUnknown=true must be true",
            CapabilityPolicy.isAllowed(CapabilityAction.APP_RATE, null, allowOnUnknown = true),
        )
        assertFalse(
            "Unknown + allowOnUnknown=false must be false",
            CapabilityPolicy.isAllowed(CapabilityAction.APP_RATE, null, allowOnUnknown = false),
        )
    }

    @Test
    fun `isAllowed defaults to true on Unknown`() {
        // The default is "optimistic — let the server decide" so that a
        // brand-new capability shipped only by the freshest server
        // versions does not silently disable affordances on every
        // client that talks to slightly-older servers.
        assertTrue(CapabilityPolicy.isAllowed(CapabilityAction.CHAT_TOPIC_CREATE, null))
        assertTrue(CapabilityPolicy.isAllowed(CapabilityAction.CHAT_TOPIC_CREATE, emptyMap()))
    }

    @Test
    fun `isAllowed treats explicitly-denied as authoritative even on a tiny map`() {
        // Single-entry map must not be misinterpreted as Unknown.
        val caps = mapOf(CapabilityAction.MODERATION_ENTER to Capability(allowed = false))
        assertFalse(
            CapabilityPolicy.isAllowed(
                action = CapabilityAction.MODERATION_ENTER,
                capabilities = caps,
                allowOnUnknown = true,
            )
        )
    }

    // --- CapabilityResult sealed hierarchy ---------------------------

    @Test
    fun `Allowed and Unknown are singletons`() {
        // sealed object semantics — important for `when` exhaustiveness
        // checks and for `assertSame` in calling tests.
        assertSame(CapabilityResult.Allowed, CapabilityResult.Allowed)
        assertSame(CapabilityResult.Unknown, CapabilityResult.Unknown)
    }

    @Test
    fun `Denied has value semantics`() {
        val cap = Capability(allowed = false, reason = CapabilityReason.OWNERSHIP)
        val a = CapabilityResult.Denied(cap)
        val b = CapabilityResult.Denied(cap)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotNull(a.toString())
    }
}
