package com.tomclaw.appsend.screen.search

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import java.util.Locale

interface SearchInteractor {

    fun searchApps(query: String, offsetAppId: String? = null): Observable<List<AppEntity>>

}

class SearchInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
    private val schedulers: SchedulersFactory
) : SearchInteractor {

    override fun searchApps(query: String, offsetAppId: String?): Observable<List<AppEntity>> {
        return api.searchApps(
            query = query,
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

