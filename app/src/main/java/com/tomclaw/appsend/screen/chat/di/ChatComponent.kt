package com.tomclaw.appsend.screen.chat.di

import com.tomclaw.appsend.screen.chat.ChatActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [ChatModule::class])
interface ChatComponent {

    fun inject(activity: ChatActivity)

}