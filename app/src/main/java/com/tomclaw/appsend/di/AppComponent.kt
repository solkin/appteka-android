package com.tomclaw.appsend.di

import com.tomclaw.appsend.screen.topics.di.TopicsComponent
import com.tomclaw.appsend.screen.topics.di.TopicsModule
import com.tomclaw.appsend.screen.moderation.di.ModerationComponent
import com.tomclaw.appsend.screen.moderation.di.ModerationModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun moderationComponent(module: ModerationModule): ModerationComponent

    fun topicsComponent(module: TopicsModule): TopicsComponent

}
