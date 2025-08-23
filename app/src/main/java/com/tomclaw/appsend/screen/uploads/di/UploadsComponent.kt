package com.tomclaw.appsend.screen.uploads.di

import com.tomclaw.appsend.screen.uploads.UploadsActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [UploadsModule::class])
interface UploadsComponent {

    fun inject(activity: UploadsActivity)

}
