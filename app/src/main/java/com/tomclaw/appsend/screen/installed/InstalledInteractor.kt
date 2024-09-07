package com.tomclaw.appsend.screen.installed

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.installed.api.CheckUpdatesRequest
import com.tomclaw.appsend.screen.installed.api.UpdateEntity
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.FileHelper.escapeFileSymbols
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.util.Locale

interface InstalledInteractor {

    fun listInstalledApps(systemApps: Boolean): Observable<List<InstalledAppEntity>>

    fun getUpdates(apps: Map<String, Long>): Observable<List<UpdateEntity>>

    fun getPackagePermissions(packageName: String): List<String>

    fun getPackageUploadInfo(packageName: String): Pair<UploadPackage, UploadApk>

    fun extractApk(
        path: String,
        label: String,
        version: String,
        packageName: String
    ): Observable<String>

}

class InstalledInteractorImpl(
    private val api: StoreApi,
    private val appsDir: File,
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

    override fun getUpdates(apps: Map<String, Long>): Observable<List<UpdateEntity>> {
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

    override fun extractApk(
        path: String,
        label: String,
        version: String,
        packageName: String
    ): Observable<String> {
        return Single
            .create { emitter ->
                try {
                    val src = File(path)
                    val dst = targetFile(label, version, packageName)
                    src.copyTo(dst, overwrite = true)
                    emitter.onSuccess(dst.path)
                } catch (ex: Throwable) {
                    emitter.onError(ex)
                }
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    private fun targetFile(label: String, version: String, packageName: String): File {
        val fileName = fileName(label, version, packageName)
        return File(appsDir, "$fileName.apk")
    }

    private fun fileName(label: String, version: String, packageName: String): String {
        return escapeFileSymbols("$label-$version-$packageName")
    }

}
