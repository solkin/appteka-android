package com.tomclaw.appsend.screen.unlink

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.unlink.api.UnlinkResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface UnlinkInteractor {

    fun unlink(appId: String, reason: String): Observable<UnlinkResponse>

}

class UnlinkInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : UnlinkInteractor {

    override fun unlink(appId: String, reason: String): Observable<UnlinkResponse> {
        return api.unlink(appId, reason)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
