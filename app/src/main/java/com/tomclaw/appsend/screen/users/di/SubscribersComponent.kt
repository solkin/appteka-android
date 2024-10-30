package com.tomclaw.appsend.screen.users.di

import com.tomclaw.appsend.screen.users.UsersFragment
import com.tomclaw.appsend.util.PerFragment
import dagger.Subcomponent

@PerFragment
@Subcomponent(modules = [SubscribersModule::class])
interface SubscribersComponent {

    fun inject(fragment: UsersFragment)

}
