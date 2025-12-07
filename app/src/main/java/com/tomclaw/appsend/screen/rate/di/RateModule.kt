package com.tomclaw.appsend.screen.rate.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.rate.RateInteractor
import com.tomclaw.appsend.screen.rate.RateInteractorImpl
import com.tomclaw.appsend.screen.rate.RatePresenter
import com.tomclaw.appsend.screen.rate.RatePresenterImpl
import com.tomclaw.appsend.screen.rate.RateResourceProvider
import com.tomclaw.appsend.screen.rate.RateResourceProviderImpl
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides
import java.util.Locale

@Module
class RateModule(
    private val context: Context,
    private val appId: String,
    private val userBrief: UserBrief,
    private val startRating: Float,
    private val startReview: String,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: RateInteractor,
        locale: Locale,
        resourceProvider: RateResourceProvider,
        schedulers: SchedulersFactory
    ): RatePresenter = RatePresenterImpl(
        appId,
        userBrief,
        startRating,
        startReview,
        interactor,
        locale,
        resourceProvider,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideResourceProvider(): RateResourceProvider = RateResourceProviderImpl(
        context.resources
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        schedulers: SchedulersFactory
    ): RateInteractor = RateInteractorImpl(api, schedulers)

}
