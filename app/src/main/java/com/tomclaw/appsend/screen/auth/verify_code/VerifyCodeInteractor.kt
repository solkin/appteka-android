package com.tomclaw.appsend.screen.auth.verify_code

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.auth.verify_code.api.VerifyCodeResponse
import com.tomclaw.appsend.user.SessionStorage
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface VerifyCodeInteractor {

    fun verifyCode(requestId: String, code: String, name: String?): Observable<VerifyCodeResponse>

}

class VerifyCodeInteractorImpl(
    private val api: StoreApi,
    private val sessionStorage: SessionStorage,
    private val schedulers: SchedulersFactory
) : VerifyCodeInteractor {

    override fun verifyCode(
        requestId: String,
        code: String,
        name: String?,
    ): Observable<VerifyCodeResponse> {
        return sessionStorage.loadSessionCredentials()
            .map { it.guid }
            .onErrorResumeNext { Single.create { it.onSuccess("") } }
            .flatMap {
                val guid = it.takeIf { it.isNotBlank() }
                api.verifyCode(requestId, code, name, guid)
            }
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
