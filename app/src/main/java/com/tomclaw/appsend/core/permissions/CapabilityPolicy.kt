package com.tomclaw.appsend.core.permissions

/**
 * Result of asking whether a specific action is allowed for a resource.
 *
 * The sealed hierarchy keeps the "unknown" case explicit — it is NOT the
 * same as "denied". Unknown happens when the server has not yet reported
 * a capability (old response, offline cache) and allows the UI to pick a
 * sensible default without silently hiding buttons.
 */
sealed class CapabilityResult {
    object Allowed : CapabilityResult()
    data class Denied(val capability: Capability) : CapabilityResult()
    object Unknown : CapabilityResult()
}

/**
 * Stateless policy that translates a [Capability] map (as received from
 * the server) plus an action key into a [CapabilityResult]. Centralised
 * so that presenters and converters never have to reach into raw maps.
 */
object CapabilityPolicy {

    fun check(action: String, capabilities: Map<String, Capability>?): CapabilityResult {
        val capability = capabilities?.get(action) ?: return CapabilityResult.Unknown
        return if (capability.allowed) {
            CapabilityResult.Allowed
        } else {
            CapabilityResult.Denied(capability)
        }
    }

    /**
     * Convenience check for boolean-only call sites (e.g. visibility of a
     * menu item). [allowOnUnknown] controls graceful degradation when the
     * capability map is missing: defaults to true so that a fresh client
     * talking to an old server still offers the same affordances as
     * before — the server will reject the actual request with 403 if
     * needed, and the existing unauthorized-error plumbing takes over.
     */
    fun isAllowed(
        action: String,
        capabilities: Map<String, Capability>?,
        allowOnUnknown: Boolean = true,
    ): Boolean = when (val result = check(action, capabilities)) {
        is CapabilityResult.Allowed -> true
        is CapabilityResult.Denied -> false
        CapabilityResult.Unknown -> allowOnUnknown
    }
}
