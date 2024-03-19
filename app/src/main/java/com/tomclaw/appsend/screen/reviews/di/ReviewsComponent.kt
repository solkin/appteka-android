package com.tomclaw.appsend.screen.reviews.di

import com.tomclaw.appsend.screen.reviews.ReviewsActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [ReviewsModule::class])
interface ReviewsComponent {

    fun inject(activity: ReviewsActivity)

}
