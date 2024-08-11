package com.tomclaw.appsend.screen.installed

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.net.AppEntry
import com.tomclaw.appsend.screen.installed.api.CheckUpdatesRequest
import com.tomclaw.appsend.screen.installed.api.CheckUpdatesResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.Locale

interface InstalledInteractor {

    fun listInstalledApps(offsetAppId: String? = null): Observable<List<InstalledAppEntity>>

    fun getUpdates(apps: Map<String, Long>): Observable<List<AppEntry>>

}

class InstalledInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
    private val infoProvider: InstalledInfoProvider,
    private val schedulers: SchedulersFactory
) : InstalledInteractor {

    override fun listInstalledApps(offsetAppId: String?): Observable<List<InstalledAppEntity>> {
        return Single
            .create { it.onSuccess(infoProvider.getInstalledApps()) }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun getUpdates(apps: Map<String, Long>): Observable<List<AppEntry>> {
        val request = CheckUpdatesRequest(
            locale = locale.language,
            apps = apps,
        )
        return api.checkUpdates(request)
            .map { response ->
                response.result.entries
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }


}
