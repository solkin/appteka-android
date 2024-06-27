package com.tomclaw.appsend.screen.about.di

import com.tomclaw.appsend.screen.about.AboutActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [AboutModule::class])
interface AboutComponent {

    fun inject(activity: AboutActivity)

}
