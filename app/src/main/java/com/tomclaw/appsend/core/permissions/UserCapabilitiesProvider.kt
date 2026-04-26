package com.tomclaw.appsend.core.permissions

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * Session-wide, in-memory holder for the global capability map fetched
 * from `/api/1/user/capabilities`. Mirrors the pattern of
 * [com.tomclaw.appsend.user.ModerationProvider] — singleton in DI,
 * pure value-store, no networking.
 *
 * UI layers ask [getCapabilities] at render time and observe
 * [observeCapabilities] for live updates after login / ACL refresh.
 * When the snapshot is missing, callers should fall back to whatever
 * legacy heuristic they had — that is what the [CapabilityResult.Unknown]
 * branch in [CapabilityPolicy] is for.
 */
interface UserCapabilitiesProvider {

    fun getCapabilities(): Map<String, Capability>?

    fun setCapabilities(capabilities: Map<String, Capability>?)

    fun clear()

    fun observeCapabilities(): Observable<Map<String, Capability>>
}

class UserCapabilitiesProviderImpl : UserCapabilitiesProvider {

    private var capabilities: Map<String, Capability>? = null
    private val subject = BehaviorSubject.create<Map<String, Capability>>()

    override fun getCapabilities(): Map<String, Capability>? = capabilities

    override fun setCapabilities(capabilities: Map<String, Capability>?) {
        this.capabilities = capabilities
        capabilities?.let { subject.onNext(it) }
    }

    override fun clear() {
        capabilities = null
        subject.onNext(emptyMap())
    }

    override fun observeCapabilities(): Observable<Map<String, Capability>> = subject
}
