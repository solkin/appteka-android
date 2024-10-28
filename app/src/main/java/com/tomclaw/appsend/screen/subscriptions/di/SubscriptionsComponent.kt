package com.tomclaw.appsend.screen.subscriptions.di

import com.tomclaw.appsend.screen.subscriptions.SubscriptionsActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [SubscriptionsModule::class])
interface SubscriptionsComponent {

    fun inject(activity: SubscriptionsActivity)

}
