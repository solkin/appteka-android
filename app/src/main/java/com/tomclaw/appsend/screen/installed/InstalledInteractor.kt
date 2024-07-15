package com.tomclaw.appsend.screen.installed

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.Locale

interface InstalledInteractor {

    fun listApps(offsetAppId: String? = null): Observable<List<AppEntity>>

}

class InstalledInteractorImpl(
    private val api: StoreApi,
    private val userId: Int,
    private val locale: Locale,
    private val schedulers: SchedulersFactory
) : InstalledInteractor {

    override fun listApps(offsetAppId: String?): Observable<List<AppEntity>> {
        return Single.create<List<AppEntity>> {  }.toObservable()
//        return api
//            .getInstalledList(
//                userId = userId,
//                appId = offsetAppId,
//                locale = locale.language
//            )
//            .map { list ->
//                list.result.files
//            }
//            .toObservable()
//            .subscribeOn(schedulers.io())
    }

}
