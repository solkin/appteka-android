package com.tomclaw.appsend.screen.distro

import android.net.Uri
import com.tomclaw.appsend.core.StreamsProvider
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File

interface DistroInteractor {

    fun listDistroApps(): Observable<List<DistroAppEntity>>

    fun getPackagePermissions(path: String): List<String>

    fun getPackageUploadInfo(packageName: String): Pair<UploadPackage, UploadApk>?

    fun removeApk(path: String): Observable<Unit>

    fun copyFile(source: String, target: Uri): Observable<Unit>

}

class DistroInteractorImpl(
    private val infoProvider: DistroInfoProvider,
    private val streamsProvider: StreamsProvider,
    private val schedulers: SchedulersFactory
) : DistroInteractor {

    override fun listDistroApps(): Observable<List<DistroAppEntity>> {
        return Single
            .create {
                val items = infoProvider.getApkItems()
                it.onSuccess(items)
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun getPackagePermissions(path: String): List<String> {
        return infoProvider.getPackagePermissions(path)
    }

    override fun getPackageUploadInfo(packageName: String): Pair<UploadPackage, UploadApk>? {
        return infoProvider.getPackageUploadInfo(packageName)
    }

    override fun removeApk(path: String): Observable<Unit> {
        return Single
            .create {
                File(path).delete()
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
