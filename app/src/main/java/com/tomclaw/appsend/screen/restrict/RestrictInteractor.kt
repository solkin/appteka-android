package com.tomclaw.appsend.screen.restrict

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.RejectionReason
import com.tomclaw.appsend.dto.SCOPE_BLOCK
import com.tomclaw.appsend.dto.SCOPE_UNPUBLISH
import com.tomclaw.appsend.screen.restrict.api.RestrictResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.Locale

/**
 * Taking an app off the store, whichever way.
 *
 * Unpublishing and blocking differ in exactly two places — the endpoint
 * they post to and the reason list they may pick from — so the screen
 * knows neither. It asks for reasons, submits a choice, and the
 * implementation injected for the entry point decides what that means.
 */
interface RestrictInteractor {

    fun reasons(): Single<List<RejectionReason>>

    fun submit(appId: String, reasonCode: Int, comment: String): Observable<RestrictResponse>

}

/** Blocking: permanent, ACL-only, and it frees the APK from storage. */
class UnlinkInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
    private val schedulers: SchedulersFactory,
) : RestrictInteractor {

    override fun reasons(): Single<List<RejectionReason>> = api
        .getRejectionReasons(locale = locale.language, scope = SCOPE_BLOCK)
        .map { it.result.reasons }
        .subscribeOn(schedulers.io())

    override fun submit(
        appId: String,
        reasonCode: Int,
        comment: String,
    ): Observable<RestrictResponse> = api
        .unlink(appId = appId, reasonCode = reasonCode, comment = comment)
        .map { it.result }
        .toObservable()
        .subscribeOn(schedulers.io())

}

/** Unpublishing: reversible — the author can fix the app and resubmit. */
class UnpublishInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
    private val schedulers: SchedulersFactory,
) : RestrictInteractor {

    override fun reasons(): Single<List<RejectionReason>> = api
        .getRejectionReasons(locale = locale.language, scope = SCOPE_UNPUBLISH)
        .map { it.result.reasons }
        .subscribeOn(schedulers.io())

    override fun submit(
        appId: String,
        reasonCode: Int,
        comment: String,
    ): Observable<RestrictResponse> = api
        .unpublish(appId = appId, reasonCode = reasonCode, comment = comment)
        .map { it.result }
        .toObservable()
        .subscribeOn(schedulers.io())

}
