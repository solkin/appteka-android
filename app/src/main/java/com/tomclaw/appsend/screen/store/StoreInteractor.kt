package com.tomclaw.appsend.screen.store

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.AppsPage
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import java.util.Locale

interface StoreInteractor {

    fun listApps(
        offsetAppId: String? = null,
        categoryId: Int? = null,
        openSource: Boolean = false,
        exclusive: Boolean = false
    ): Observable<AppsPage>

}

class StoreInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
    private val schedulers: SchedulersFactory
) : StoreInteractor {

    override fun listApps(
        offsetAppId: String?,
        categoryId: Int?,
        openSource: Boolean,
        exclusive: Boolean
    ): Observable<AppsPage> {
        return api.getTopList(
            appId = offsetAppId,
            locale = locale.language,
            categoryId = categoryId,
            openSource = true.takeIf { openSource },
            exclusive = true.takeIf { exclusive }
        )
            .map { list ->
                AppsPage(
                    entries = list.result.files,
                    contentFilter = list.result.contentFilter.orEmpty(),
                )
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
