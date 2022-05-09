package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface DetailsInteractor {

    fun loadDetails(appId: String?, packageName: String?): Observable<Details>

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

}