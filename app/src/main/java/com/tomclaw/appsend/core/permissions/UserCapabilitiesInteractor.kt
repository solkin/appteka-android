package com.tomclaw.appsend.core.permissions

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.core.permissions.api.UserCapabilitiesResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Single

/**
 * Loads the global capability snapshot from the server and pushes it
 * into [UserCapabilitiesProvider]. Single responsibility — networking +
 * cache write; the holder owns the in-memory state and observation.
 *
 * `refresh()` returns the fresh response so callers that want to chain
 * UI logic on a successful refresh can do so. Side-effect (provider
 * update) happens via `doOnSuccess` so a passive ignore-the-result call
 * still updates global state.
 */
interface UserCapabilitiesInteractor {

    fun refresh(): Single<UserCapabilitiesResponse>
}

class UserCapabilitiesInteractorImpl(
    private val api: StoreApi,
    private val provider: UserCapabilitiesProvider,
    private val schedulers: SchedulersFactory,
) : UserCapabilitiesInteractor {

    override fun refresh(): Single<UserCapabilitiesResponse> {
        return api.getUserCapabilities()
            .map { it.result }
            .doOnSuccess { provider.setCapabilities(it.capabilities) }
            .subscribeOn(schedulers.io())
    }
}
