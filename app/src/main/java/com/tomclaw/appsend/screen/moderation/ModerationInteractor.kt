package com.tomclaw.appsend.screen.moderation

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import java.util.Locale

interface ModerationInteractor {

    fun listApps(offsetAppId: String? = null): Observable<List<AppEntity>>

}

class ModerationInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
    private val userDataInteractor: UserDataInteractor,
    private val schedulers: SchedulersFactory
) : ModerationInteractor {

    override fun listApps(offsetAppId: String?): Observable<List<AppEntity>> {
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.getModerationList(
                    guid = it.guid,
                    appId = offsetAppId,
                    locale = locale.language
                )
            }
            .map { list ->
                list.result.files
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}