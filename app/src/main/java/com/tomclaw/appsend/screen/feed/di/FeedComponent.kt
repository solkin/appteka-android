package com.tomclaw.appsend.screen.feed.di

import com.tomclaw.appsend.screen.feed.FeedFragment
import com.tomclaw.appsend.util.PerFragment
import dagger.Subcomponent

@PerFragment
@Subcomponent(modules = [FeedModule::class])
interface FeedComponent {

    fun inject(fragment: FeedFragment)

}
