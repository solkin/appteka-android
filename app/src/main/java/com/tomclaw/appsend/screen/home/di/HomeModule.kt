package com.tomclaw.appsend.screen.home.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.core.AppInfoProvider
import com.tomclaw.appsend.core.StandByApi
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.home.HomeInteractor
import com.tomclaw.appsend.screen.home.HomeInteractorImpl
import com.tomclaw.appsend.screen.home.HomePresenter
import com.tomclaw.appsend.screen.home.HomePresenterImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides
import java.util.Locale

@Module
class HomeModule(
    private val context: Context,
    private val startAction: String?,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: HomeInteractor,
        schedulers: SchedulersFactory
    ): HomePresenter = HomePresenterImpl(
        startAction,
        interactor,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        storeApi: StoreApi,
        standByApi: StandByApi,
        locale: Locale,
        appInfoProvider: AppInfoProvider,
        schedulers: SchedulersFactory
    ): HomeInteractor = HomeInteractorImpl(
        storeApi,
        standByApi,
        locale,
        appInfoProvider,
        schedulers
    )

}
