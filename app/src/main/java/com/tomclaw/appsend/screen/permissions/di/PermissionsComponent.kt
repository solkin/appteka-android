package com.tomclaw.appsend.screen.permissions.di

import com.tomclaw.appsend.screen.permissions.PermissionsActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [PermissionsModule::class])
interface PermissionsComponent {

    fun inject(activity: PermissionsActivity)

}
