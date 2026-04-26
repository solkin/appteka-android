package com.tomclaw.appsend.core.permissions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Properties of [Capability] / [CapabilityReason] that the rest of
 * the system silently relies on. Most are trivial Kotlin/Gson defaults
 * — pinning them here makes any future "let's flatten the DTO" change
 * loud.
 */
class CapabilityTest {

    @Test
    fun `optional fields default to null`() {
        val c = Capability(allowed = true)
        assertNull(c.reason)
        assertNull(c.blockedBy)
        assertNull(c.hintKey)
    }

    @Test
    fun `equality is structural across all fields`() {
        val a = Capability(
            allowed = false,
            reason = CapabilityReason.RULE,
            blockedBy = AccessRule.READ_ONLY_MESSAGES,
            hintKey = "cap.reason.read_only_messages",
        )
        val b = Capability(
            allowed = false,
            reason = CapabilityReason.RULE,
            blockedBy = AccessRule.READ_ONLY_MESSAGES,
            hintKey = "cap.reason.read_only_messages",
        )
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `differing in any single field breaks equality`() {
        val base = Capability(
            allowed = false,
            reason = CapabilityReason.RULE,
            blockedBy = AccessRule.READ_ONLY_MESSAGES,
            hintKey = "cap.reason.read_only_messages",
        )
        assertNotEquals(base, base.copy(allowed = true))
        assertNotEquals(base, base.copy(reason = CapabilityReason.OWNERSHIP))
        assertNotEquals(base, base.copy(blockedBy = AccessRule.READ_ONLY_RATINGS))
        assertNotEquals(base, base.copy(hintKey = "other"))
    }

    @Test
    fun `reason codes match the wire format expected by the backend`() {
        // These four strings are part of the stable JSON contract with
        // the Go server — see common/capabilities/capability.go. Any
        // change here MUST be coordinated with the backend or the UI
        // will start ignoring deny reasons.
        assertEquals("rule", CapabilityReason.RULE)
        assertEquals("role", CapabilityReason.ROLE)
        assertEquals("auth", CapabilityReason.AUTH)
        assertEquals("ownership", CapabilityReason.OWNERSHIP)
    }

    @Test
    fun `Capability is parcelable-friendly — no impossible field combinations`() {
        // Reason without blockedBy should still be a valid Capability —
        // the server omits blockedBy for non-rule reasons.
        val c = Capability(allowed = false, reason = CapabilityReason.AUTH)
        assertEquals(false, c.allowed)
        assertEquals(CapabilityReason.AUTH, c.reason)
        assertNull(c.blockedBy)
    }

    @Test
    fun `allowed=true with denial-side fields populated still equals only itself`() {
        // Edge case: server bug or future migration could ship
        // {"allowed": true, "blocked_by": "...something..."}. We do
        // not silently massage it; the policy treats allowed=true as
        // Allowed and the extra fields are preserved on the value
        // object for diagnostic tools.
        val weird = Capability(
            allowed = true,
            reason = CapabilityReason.RULE,
            blockedBy = AccessRule.AUTOMODERATION,
        )
        val plain = Capability(allowed = true)
        assertNotEquals(weird, plain)
        assertTrue(weird.allowed)
    }
}
