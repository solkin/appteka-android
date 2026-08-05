package com.tomclaw.appsend.screen.restrict.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.restrict.RestrictInteractor
import com.tomclaw.appsend.screen.restrict.RestrictMode
import com.tomclaw.appsend.screen.restrict.RestrictPresenter
import com.tomclaw.appsend.screen.restrict.RestrictPresenterImpl
import com.tomclaw.appsend.screen.restrict.RestrictResourceProvider
import com.tomclaw.appsend.screen.restrict.UnlinkInteractorImpl
import com.tomclaw.appsend.screen.restrict.UnlinkResourceProvider
import com.tomclaw.appsend.screen.restrict.UnpublishInteractorImpl
import com.tomclaw.appsend.screen.restrict.UnpublishResourceProvider
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides
import java.util.Locale

/**
 * The only place that knows blocking from unpublishing: it picks the
 * interactor (endpoint + reason scope) and the resource provider
 * (wording). The screen above is built from these two and stays
 * mode-blind.
 */
@Module
class RestrictModule(
    private val context: Context,
    private val appId: String,
    private val mode: RestrictMode,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: RestrictInteractor,
        resourceProvider: RestrictResourceProvider,
        schedulers: SchedulersFactory
    ): RestrictPresenter =
        RestrictPresenterImpl(appId, interactor, resourceProvider, schedulers, state)

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        locale: Locale,
        schedulers: SchedulersFactory
    ): RestrictInteractor = when (mode) {
        RestrictMode.BLOCK -> UnlinkInteractorImpl(api, locale, schedulers)
        RestrictMode.UNPUBLISH -> UnpublishInteractorImpl(api, locale, schedulers)
    }

    @Provides
    @PerActivity
    internal fun provideResourceProvider(): RestrictResourceProvider = when (mode) {
        RestrictMode.BLOCK -> UnlinkResourceProvider(context.resources)
        RestrictMode.UNPUBLISH -> UnpublishResourceProvider(context.resources)
    }

}
