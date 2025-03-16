package com.tomclaw.appsend.screen.post.di

import com.tomclaw.appsend.screen.post.PostActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [PostModule::class])
interface PostComponent {

    fun inject(activity: PostActivity)

}
