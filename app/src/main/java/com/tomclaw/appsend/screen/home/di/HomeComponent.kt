package com.tomclaw.appsend.screen.home.di

import com.tomclaw.appsend.screen.home.HomeActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [HomeModule::class])
interface HomeComponent {

    fun inject(activity: HomeActivity)

}
