package com.tomclaw.appsend.screen.moderation.di

import com.tomclaw.appsend.screen.moderation.ModerationActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [ModerationModule::class])
interface ModerationComponent {

    fun inject(activity: ModerationActivity)

}
