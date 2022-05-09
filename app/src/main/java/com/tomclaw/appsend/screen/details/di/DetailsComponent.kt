package com.tomclaw.appsend.screen.details.di

import com.tomclaw.appsend.screen.details.DetailsActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [DetailsModule::class])
interface DetailsComponent {

    fun inject(activity: DetailsActivity)

}