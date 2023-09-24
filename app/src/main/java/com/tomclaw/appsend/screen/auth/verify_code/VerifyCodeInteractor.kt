package com.tomclaw.appsend.screen.auth.verify_code

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.auth.verify_code.api.VerifyCodeResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface VerifyCodeInteractor {

    fun verifyCode(requestId: String, code: String, name: String?): Observable<VerifyCodeResponse>

}

class VerifyCodeInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : VerifyCodeInteractor {

    override fun verifyCode(
        requestId: String,
        code: String,
        name: String?,
    ): Observable<VerifyCodeResponse> {
        return api.verifyCode(requestId, code, name)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
