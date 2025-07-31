package com.tomclaw.appsend.screen.unlink.di

import com.tomclaw.appsend.screen.unlink.UnlinkActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [UnlinkModule::class])
interface UnlinkComponent {

    fun inject(activity: UnlinkActivity)

}
