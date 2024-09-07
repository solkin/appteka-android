package com.tomclaw.appsend.screen.distro

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.util.Locale

interface DistroInteractor {

    fun listDistroApps(): Observable<List<DistroAppEntity>>

    fun getPackagePermissions(packageName: String): List<String>

}

class DistroInteractorImpl(
    private val api: StoreApi,
    private val appsDir: File,
    private val locale: Locale,
    private val infoProvider: DistroInfoProvider,
    private val schedulers: SchedulersFactory
) : DistroInteractor {

    override fun listDistroApps(): Observable<List<DistroAppEntity>> {
        return Single
            .create {
                it.onSuccess(emptyList<DistroAppEntity>())
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun getPackagePermissions(packageName: String): List<String> {
        return infoProvider.getPackagePermissions(packageName)
    }

}
