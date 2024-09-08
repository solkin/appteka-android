package com.tomclaw.appsend.screen.distro

import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File

interface DistroInteractor {

    fun listDistroApps(): Observable<List<DistroAppEntity>>

    fun getPackagePermissions(path: String): List<String>

    fun removeApk(path: String): Observable<Unit>

}

class DistroInteractorImpl(
    private val infoProvider: DistroInfoProvider,
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

    override fun removeApk(path: String): Observable<Unit> {
        return Single
            .create {
                File(path).delete()
                it.onSuccess(Unit)
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
