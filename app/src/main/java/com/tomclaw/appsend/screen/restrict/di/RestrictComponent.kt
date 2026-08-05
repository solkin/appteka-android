package com.tomclaw.appsend.screen.restrict.di

import com.tomclaw.appsend.screen.restrict.RestrictActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [RestrictModule::class])
interface RestrictComponent {

    fun inject(activity: RestrictActivity)

}
