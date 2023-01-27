package com.tomclaw.appsend.screen.upload.di

import com.tomclaw.appsend.screen.upload.UploadActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [UploadModule::class])
interface UploadComponent {

    fun inject(activity: UploadActivity)

}