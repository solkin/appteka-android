package com.tomclaw.appsend.core.permissions

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.core.permissions.api.UserCapabilitiesResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject

/** Loads the session capability snapshot; cache writes and resets run on the main thread. */
interface UserCapabilitiesInteractor {

    fun refresh(): Single<UserCapabilitiesResponse>

    fun reset()
}

class UserCapabilitiesInteractorImpl(
    private val api: StoreApi,
    private val provider: UserCapabilitiesProvider,
    private val schedulers: SchedulersFactory,
) : UserCapabilitiesInteractor {

    private val sessionChanges = PublishSubject.create<Unit>()

    override fun refresh(): Single<UserCapabilitiesResponse> {
        return api.getUserCapabilities()
            .map { it.result }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.mainThread())
            .takeUntil(sessionChanges.firstOrError())
            .doOnSuccess { provider.setCapabilities(it.capabilities) }
    }

    override fun reset() {
        sessionChanges.onNext(Unit)
        provider.clear()
    }
}
