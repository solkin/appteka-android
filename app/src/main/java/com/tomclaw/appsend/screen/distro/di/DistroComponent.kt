package com.tomclaw.appsend.screen.distro.di

import com.tomclaw.appsend.screen.distro.DistroActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [DistroModule::class])
interface DistroComponent {

    fun inject(activity: DistroActivity)

}
