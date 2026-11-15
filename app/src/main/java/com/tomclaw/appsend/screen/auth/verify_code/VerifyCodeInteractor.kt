package com.tomclaw.appsend.screen.auth.verify_code

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.core.permissions.UserCapabilitiesInteractor
import com.tomclaw.appsend.screen.auth.verify_code.api.VerifyCodeResponse
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.logDebug
import io.reactivex.rxjava3.core.Observable

interface VerifyCodeInteractor {

    fun verifyCode(requestId: String, code: String, name: String?): Observable<VerifyCodeResponse>

}

class VerifyCodeInteractorImpl(
    private val api: StoreApi,
    private val capabilitiesInteractor: UserCapabilitiesInteractor,
    private val schedulers: SchedulersFactory
) : VerifyCodeInteractor {

    override fun verifyCode(
        requestId: String,
        code: String,
        name: String?,
    ): Observable<VerifyCodeResponse> {
        return api.verifyCode(requestId, code, name)
            .map { it.result }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.mainThread())
            .flatMap { response ->
                capabilitiesInteractor.reset()
                capabilitiesInteractor.refresh()
                    .map { response }
                    .onErrorReturn { error ->
                        logDebug("Capabilities unavailable after login: ${error.javaClass.simpleName}")
                        response
                    }
            }
            .toObservable()
    }

}
