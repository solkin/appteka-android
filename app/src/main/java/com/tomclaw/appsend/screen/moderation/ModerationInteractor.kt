package com.tomclaw.appsend.screen.moderation

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.net.UserData
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import java.util.*

interface ModerationInteractor {

    fun listApps(offsetAppId: String? = null): Observable<List<AppEntity>>

}

class ModerationInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
    private val userData: UserData,
    private val schedulers: SchedulersFactory
) : ModerationInteractor {

    override fun listApps(offsetAppId: String?): Observable<List<AppEntity>> {
        return api
            .getModerationList(
                guid = userData.guid,
                appId = offsetAppId,
                locale = locale.language
            )
            .map { list ->
                list.result.files
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}