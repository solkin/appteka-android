package com.tomclaw.appsend.screen.subscribers.di

import com.tomclaw.appsend.screen.subscribers.SubscribersFragment
import com.tomclaw.appsend.util.PerFragment
import dagger.Subcomponent

@PerFragment
@Subcomponent(modules = [SubscribersModule::class])
interface SubscribersComponent {

    fun inject(fragment: SubscribersFragment)

}
