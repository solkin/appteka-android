package com.tomclaw.appsend.upload.di

import com.tomclaw.appsend.upload.UploadService
import com.tomclaw.appsend.util.PerService
import dagger.Subcomponent

@PerService
@Subcomponent(modules = [UploadServiceModule::class])
interface UploadServiceComponent {

    fun inject(service: UploadService)

}