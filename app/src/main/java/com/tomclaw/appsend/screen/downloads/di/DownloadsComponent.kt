package com.tomclaw.appsend.screen.downloads.di

import com.tomclaw.appsend.screen.downloads.DownloadsActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [DownloadsModule::class])
interface DownloadsComponent {

    fun inject(activity: DownloadsActivity)

}
