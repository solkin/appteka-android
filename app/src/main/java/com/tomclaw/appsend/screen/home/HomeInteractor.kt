package com.tomclaw.appsend.screen.home

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.home.api.StartupResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface HomeInteractor {

    fun loadStartup(): Observable<StartupResponse>

}

class HomeInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : HomeInteractor {

    override fun loadStartup(): Observable<StartupResponse> {
        return api
            .getStartup()
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
