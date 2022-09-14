package com.tomclaw.appsend.screen.rate.di

import com.tomclaw.appsend.screen.rate.RateActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [RateModule::class])
interface RateComponent {

    fun inject(activity: RateActivity)

}
