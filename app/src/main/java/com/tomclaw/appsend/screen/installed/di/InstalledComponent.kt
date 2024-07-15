package com.tomclaw.appsend.screen.installed.di

import com.tomclaw.appsend.screen.installed.InstalledActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [InstalledModule::class])
interface InstalledComponent {

    fun inject(activity: InstalledActivity)

}
