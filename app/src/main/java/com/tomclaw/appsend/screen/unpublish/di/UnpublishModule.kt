package com.tomclaw.appsend.screen.unpublish.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.unpublish.UnpublishInteractor
import com.tomclaw.appsend.screen.unpublish.UnpublishInteractorImpl
import com.tomclaw.appsend.screen.unpublish.UnpublishPresenter
import com.tomclaw.appsend.screen.unpublish.UnpublishPresenterImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides

@Module
class UnpublishModule(
    private val context: Context,
    private val appId: String,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: UnpublishInteractor,
        schedulers: SchedulersFactory
    ): UnpublishPresenter = UnpublishPresenterImpl(appId, interactor, schedulers, state)

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        schedulers: SchedulersFactory
    ): UnpublishInteractor = UnpublishInteractorImpl(api, schedulers)

}
