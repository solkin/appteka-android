package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.details.api.DeletionResponse
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.screen.details.api.ModerationDecisionResponse
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface DetailsInteractor {

    fun loadDetails(appId: String?, packageName: String?): Observable<Details>

    fun sendModerationDecision(
        appId: String,
        isApprove: Boolean
    ): Single<ModerationDecisionResponse>

    fun deleteApplication(appId: String): Single<DeletionResponse>

}

class DetailsInteractorImpl(
    private val userDataInteractor: UserDataInteractor,
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : DetailsInteractor {

    override fun loadDetails(appId: String?, packageName: String?): Observable<Details> {
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.getInfo(
                    guid = it.guid,
                    appId = appId,
                    packageName = packageName,
                )
            }
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun sendModerationDecision(
        appId: String,
        isApprove: Boolean
    ): Single<ModerationDecisionResponse> {
        val decision = if (isApprove) MODERATION_APPROVE else MODERATION_DENY
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.sendModerationDecision(
                    guid = it.guid,
                    appId = appId,
                    decision = decision,
                )
            }
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

    override fun deleteApplication(appId: String): Single<DeletionResponse> {
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.deleteApplication(
                    guid = it.guid,
                    appId = appId,
                )
            }
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

}

const val MODERATION_APPROVE = 1
const val MODERATION_DENY = -1
