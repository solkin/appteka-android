package com.tomclaw.appsend.screen.avatar_crop.di

import com.tomclaw.appsend.screen.avatar_crop.AvatarCropActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [AvatarCropModule::class])
interface AvatarCropComponent {

    fun inject(activity: AvatarCropActivity)

}
