package com.tomclaw.appsend.screen.home

import com.tomclaw.appsend.core.AppInfoProvider
import com.tomclaw.appsend.core.StandByApi
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.home.api.StartupResponse
import com.tomclaw.appsend.screen.home.api.StatusResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import java.util.Locale

interface HomeInteractor {

    fun loadStartup(): Observable<StartupResponse>

    fun loadStatus(): Observable<StatusResponse>

}

class HomeInteractorImpl(
    private val storeApi: StoreApi,
    private val standByApi: StandByApi,
    private val locale: Locale,
    private val appInfoProvider: AppInfoProvider,
    private val schedulers: SchedulersFactory
) : HomeInteractor {

    override fun loadStartup(): Observable<StartupResponse> {
        return storeApi
            .getStartup()
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun loadStatus(): Observable<StatusResponse> {
        return standByApi
            .getStatus(locale.language, appInfoProvider.getVersionCode())
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
