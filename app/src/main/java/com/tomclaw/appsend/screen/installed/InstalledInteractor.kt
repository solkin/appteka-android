package com.tomclaw.appsend.screen.installed

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.net.AppEntry
import com.tomclaw.appsend.screen.installed.api.CheckUpdatesRequest
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.Locale

interface InstalledInteractor {

    fun listInstalledApps(systemApps: Boolean): Observable<List<InstalledAppEntity>>

    fun getUpdates(apps: Map<String, Long>): Observable<List<AppEntry>>

    fun getPackagePermissions(packageName: String): List<String>

    fun getPackageUploadInfo(packageName: String): Pair<UploadPackage, UploadApk>

}

class InstalledInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
    private val infoProvider: InstalledInfoProvider,
    private val schedulers: SchedulersFactory
) : InstalledInteractor {

    override fun listInstalledApps(systemApps: Boolean): Observable<List<InstalledAppEntity>> {
        return Single
            .create {
                it.onSuccess(
                    infoProvider
                        .getInstalledApps()
                        .filter { app -> app.isUserApp || systemApps })
            }
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
                response.result.entries ?: emptyList()
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun getPackagePermissions(packageName: String): List<String> {
        return infoProvider.getPackagePermissions(packageName)
    }

    override fun getPackageUploadInfo(packageName: String): Pair<UploadPackage, UploadApk> {
        return infoProvider.getPackageUploadInfo(packageName)
    }

}
