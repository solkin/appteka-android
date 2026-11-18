package com.tomclaw.appsend.screen.distro

import android.net.Uri
import com.tomclaw.appsend.core.StreamsProvider
import com.tomclaw.appsend.download.ApkStorage
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.EditionKeyProvider
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File

interface DistroInteractor {

    fun listDistroApps(): Observable<List<DistroAppEntity>>

    fun getPackagePermissions(fileName: String): List<String>

    fun getPackageUploadInfo(fileName: String): Pair<UploadPackage, UploadApk>?

    fun removeApk(fileName: String): Observable<Unit>

    fun copyFile(source: String, target: Uri): Observable<Unit>

    /** The APK's edition key; empty if it has none or the file is gone. */
    fun getEditionKey(fileName: String): Maybe<String>

}

class DistroInteractorImpl(
    private val infoProvider: DistroInfoProvider,
    private val apkStorage: ApkStorage,
    private val streamsProvider: StreamsProvider,
    private val editionKeyProvider: EditionKeyProvider,
    private val schedulers: SchedulersFactory
) : DistroInteractor {

    override fun getEditionKey(fileName: String): Maybe<String> {
        // Reading the signing block verifies the whole file: off the main thread.
        return Maybe
            .fromCallable<String> {
                apkStorage.getFilePath(fileName)?.let { editionKeyProvider.archiveKey(it) }
            }
            .subscribeOn(schedulers.io())
    }

    override fun listDistroApps(): Observable<List<DistroAppEntity>> {
        return Single
            .create {
                val items = infoProvider.getApkItems()
                it.onSuccess(items)
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun getPackagePermissions(fileName: String): List<String> {
        return infoProvider.getPackagePermissions(fileName)
    }

    override fun getPackageUploadInfo(fileName: String): Pair<UploadPackage, UploadApk>? {
        return infoProvider.getPackageUploadInfo(fileName)
    }

    override fun removeApk(fileName: String): Observable<Unit> {
        return Single
            .create {
                apkStorage.delete(fileName)
                it.onSuccess(Unit)
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun copyFile(source: String, target: Uri): Observable<Unit> {
        return Single
            .create { emitter ->
                try {
                    val srcFile = File(source)
                    streamsProvider.openInputStream(Uri.fromFile(srcFile))?.let { input ->
                        streamsProvider.openOutputStream(target)?.let { output ->
                            input.copyTo(output)
                            output.flush()
                            emitter.onSuccess(Unit)
                        } ?: emitter.onError(Throwable("Output stream opening error"))
                    } ?: emitter.onError(Throwable("Input stream opening error"))
                } catch (ex: Throwable) {
                    emitter.onError(ex)
                }
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
