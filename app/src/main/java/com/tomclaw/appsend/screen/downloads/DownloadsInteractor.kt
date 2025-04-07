package com.tomclaw.appsend.screen.downloads

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import java.util.Locale

interface DownloadsInteractor {

    fun listApps(offsetAppId: String? = null): Observable<List<AppEntity>>

}

class DownloadsInteractorImpl(
    private val api: StoreApi,
    private val userId: Int,
    private val locale: Locale,
    private val schedulers: SchedulersFactory
) : DownloadsInteractor {

    override fun listApps(offsetAppId: String?): Observable<List<AppEntity>> {
        return api
            .getDownloadsList(
                userId = userId,
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
