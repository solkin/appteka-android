package com.tomclaw.appsend.screen.subscribers.di

import com.tomclaw.appsend.screen.subscribers.SubscribersFragment
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [SubscribersModule::class])
interface SubscribersComponent {

    fun inject(fragment: SubscribersFragment)

}
