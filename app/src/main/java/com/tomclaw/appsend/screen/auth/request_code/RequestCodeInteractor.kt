package com.tomclaw.appsend.screen.auth.request_code

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.auth.request_code.api.RequestCodeResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface RequestCodeInteractor {

    fun requestCode(email: String): Observable<RequestCodeResponse>

}

class RequestCodeInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : RequestCodeInteractor {

    override fun requestCode(
        email: String,
    ): Observable<RequestCodeResponse> {
        return api.requestCode(email)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
