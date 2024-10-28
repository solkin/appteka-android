package com.tomclaw.appsend.screen.subscriptions.di

import android.os.Bundle
import com.tomclaw.appsend.screen.subscriptions.SubscriptionsPresenter
import com.tomclaw.appsend.screen.subscriptions.SubscriptionsPresenterImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides

@Module
class SubscriptionsModule(
    private val userId: Int,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        schedulers: SchedulersFactory
    ): SubscriptionsPresenter = SubscriptionsPresenterImpl(
        schedulers,
        state
    )

}
