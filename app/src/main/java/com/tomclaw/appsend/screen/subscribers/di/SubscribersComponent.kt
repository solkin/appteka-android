package com.tomclaw.appsend.screen.subscribers.di

import com.tomclaw.appsend.screen.subscribers.SubscribersActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [SubscribersModule::class])
interface SubscribersComponent {

    fun inject(activity: SubscribersActivity)

}
