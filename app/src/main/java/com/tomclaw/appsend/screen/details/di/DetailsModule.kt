package com.tomclaw.appsend.screen.details.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.details.DetailsInteractor
import com.tomclaw.appsend.screen.details.DetailsInteractorImpl
import com.tomclaw.appsend.screen.details.DetailsPresenter
import com.tomclaw.appsend.screen.details.DetailsPresenterImpl
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides

@Module
class DetailsModule(
    private val appId: String?,
    private val packageName: String?,
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: DetailsInteractor,
        schedulers: SchedulersFactory
    ): DetailsPresenter = DetailsPresenterImpl(appId, packageName, interactor, schedulers, state)

    @Provides
    @PerActivity
    internal fun provideInteractor(
        userDataInteractor: UserDataInteractor,
        api: StoreApi,
        schedulers: SchedulersFactory
    ): DetailsInteractor = DetailsInteractorImpl(userDataInteractor, api, schedulers)

}