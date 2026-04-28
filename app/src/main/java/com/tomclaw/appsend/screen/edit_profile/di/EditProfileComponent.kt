package com.tomclaw.appsend.screen.edit_profile.di

import com.tomclaw.appsend.screen.edit_profile.EditProfileActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [EditProfileModule::class])
interface EditProfileComponent {

    fun inject(activity: EditProfileActivity)

}
