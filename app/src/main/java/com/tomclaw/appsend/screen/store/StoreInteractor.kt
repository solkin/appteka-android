package com.tomclaw.appsend.screen.store

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import java.util.Locale

interface StoreInteractor {

    fun listApps(offsetAppId: String? = null, categoryId: Int? = null): Observable<List<AppEntity>>

}

class StoreInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
    private val schedulers: SchedulersFactory
) : StoreInteractor {

    override fun listApps(offsetAppId: String?, categoryId: Int?): Observable<List<AppEntity>> {
        return if (categoryId != null) {
            api.getTopListByCategory(
                appId = offsetAppId,
                categoryId = categoryId,
                locale = locale.language
            )
        } else {
            api.getTopList(
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