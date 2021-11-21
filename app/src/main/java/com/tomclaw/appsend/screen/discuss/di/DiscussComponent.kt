package com.tomclaw.appsend.screen.discuss.di

import com.tomclaw.appsend.screen.discuss.DiscussFragment
import com.tomclaw.appsend.util.PerFragment
import dagger.Subcomponent

@PerFragment
@Subcomponent(modules = [DiscussModule::class])
interface DiscussComponent {

    fun inject(fragment: DiscussFragment)

}