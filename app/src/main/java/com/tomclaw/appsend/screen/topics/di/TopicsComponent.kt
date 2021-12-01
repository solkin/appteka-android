package com.tomclaw.appsend.screen.topics.di

import com.tomclaw.appsend.screen.topics.TopicsFragment
import com.tomclaw.appsend.util.PerFragment
import dagger.Subcomponent

@PerFragment
@Subcomponent(modules = [TopicsModule::class])
interface TopicsComponent {

    fun inject(fragment: TopicsFragment)

}