package com.tomclaw.appsend.download.di

import com.tomclaw.appsend.download.DownloadService
import com.tomclaw.appsend.util.PerService
import dagger.Subcomponent

@PerService
@Subcomponent(modules = [DownloadServiceModule::class])
interface DownloadServiceComponent {

    fun inject(service: DownloadService)

}