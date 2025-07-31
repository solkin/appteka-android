package com.tomclaw.appsend.screen.unlink.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.screen.unlink.UnlinkInteractor
import com.tomclaw.appsend.screen.unlink.UnlinkInteractorImpl
import com.tomclaw.appsend.screen.unlink.UnlinkPresenter
import com.tomclaw.appsend.screen.unlink.UnlinkPresenterImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides

@Module
class UnlinkModule(
        private val context: Context,
        private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
            interactor: UnlinkInteractor,
            schedulers: SchedulersFactory
    ): UnlinkPresenter = UnlinkPresenterImpl(interactor, schedulers, state)

    @Provides
    @PerActivity
    internal fun provideInteractor(
            schedulers: SchedulersFactory
    ): UnlinkInteractor = UnlinkInteractorImpl(schedulers)

}
