package com.tomclaw.appsend.screen.bdui.di

import com.tomclaw.appsend.screen.bdui.BduiScreenActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [BduiScreenModule::class])
interface BduiScreenComponent {

    fun inject(activity: BduiScreenActivity)

}

