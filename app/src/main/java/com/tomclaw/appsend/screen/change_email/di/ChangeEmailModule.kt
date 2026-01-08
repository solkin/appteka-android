package com.tomclaw.appsend.screen.change_email.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.change_email.ChangeEmailInteractor
import com.tomclaw.appsend.screen.change_email.ChangeEmailInteractorImpl
import com.tomclaw.appsend.screen.change_email.ChangeEmailPresenter
import com.tomclaw.appsend.screen.change_email.ChangeEmailPresenterImpl
import com.tomclaw.appsend.screen.change_email.ChangeEmailResourceProvider
import com.tomclaw.appsend.screen.change_email.ChangeEmailResourceProviderImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides

@Module
class ChangeEmailModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        resourceProvider: ChangeEmailResourceProvider,
        interactor: ChangeEmailInteractor,
        schedulers: SchedulersFactory
    ): ChangeEmailPresenter = ChangeEmailPresenterImpl(
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
    ): ChangeEmailInteractor = ChangeEmailInteractorImpl(api, schedulers)

    @Provides
    @PerActivity
    internal fun provideResourceProvider(): ChangeEmailResourceProvider =
        ChangeEmailResourceProviderImpl(context.resources)

}
