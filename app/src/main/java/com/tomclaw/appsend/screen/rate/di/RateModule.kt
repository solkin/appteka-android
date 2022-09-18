package com.tomclaw.appsend.screen.rate.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.rate.RateInteractor
import com.tomclaw.appsend.screen.rate.RateInteractorImpl
import com.tomclaw.appsend.screen.rate.RatePresenter
import com.tomclaw.appsend.screen.rate.RatePresenterImpl
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import java.util.Locale

@Module
class RateModule(
    private val context: Context,
    private val appId: String,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: RateInteractor,
        userDataInteractor: UserDataInteractor,
        locale: Locale,
        schedulers: SchedulersFactory
    ): RatePresenter = RatePresenterImpl(
        appId,
        interactor,
        userDataInteractor,
        locale,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        locale: Locale,
        userDataInteractor: UserDataInteractor,
        schedulers: SchedulersFactory
    ): RateInteractor = RateInteractorImpl(api, userDataInteractor, schedulers)

}
