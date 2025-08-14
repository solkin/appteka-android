package com.tomclaw.appsend.screen.unpublish.di

import com.tomclaw.appsend.screen.unpublish.UnpublishActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [UnpublishModule::class])
interface UnpublishComponent {

    fun inject(activity: UnpublishActivity)

}
