package com.tomclaw.appsend.screen.ratings.di

import com.tomclaw.appsend.screen.ratings.RatingsActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [RatingsModule::class])
interface RatingsComponent {

    fun inject(activity: RatingsActivity)

}
