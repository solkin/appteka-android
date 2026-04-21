package com.tomclaw.appsend.screen.create_chat.di

import com.tomclaw.appsend.screen.create_chat.CreateChatActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [CreateChatModule::class])
interface CreateChatComponent {

    fun inject(activity: CreateChatActivity)

}
