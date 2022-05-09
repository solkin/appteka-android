package com.tomclaw.appsend.screen.details.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.screen.details.DetailsInteractor
import com.tomclaw.appsend.screen.details.DetailsInteractorImpl
import com.tomclaw.appsend.screen.details.DetailsPresenter
import com.tomclaw.appsend.screen.details.DetailsPresenterImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides

@Module
class DetailsModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: DetailsInteractor,
        schedulers: SchedulersFactory
    ): DetailsPresenter = DetailsPresenterImpl(interactor, schedulers, state)

    @Provides
    @PerActivity
    internal fun provideInteractor(
        schedulers: SchedulersFactory
    ): DetailsInteractor = DetailsInteractorImpl(schedulers)

}