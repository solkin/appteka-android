package com.tomclaw.appsend.screen.auth.request_code.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.auth.request_code.RequestCodeInteractor
import com.tomclaw.appsend.screen.auth.request_code.RequestCodeInteractorImpl
import com.tomclaw.appsend.screen.auth.request_code.RequestCodePresenter
import com.tomclaw.appsend.screen.auth.request_code.RequestCodePresenterImpl
import com.tomclaw.appsend.screen.auth.request_code.RequestCodeResourceProvider
import com.tomclaw.appsend.screen.auth.request_code.RequestCodeResourceProviderImpl
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides
import java.util.Locale

@Module
class RequestCodeModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        resourceProvider: RequestCodeResourceProvider,
        interactor: RequestCodeInteractor,
        schedulers: SchedulersFactory
    ): RequestCodePresenter = RequestCodePresenterImpl(
        resourceProvider,
        interactor,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        schedulers: SchedulersFactory
    ): RequestCodeInteractor = RequestCodeInteractorImpl(api, schedulers)

    @Provides
    @PerActivity
    internal fun provideResourceProvider(): RequestCodeResourceProvider = RequestCodeResourceProviderImpl(context.resources)

}
