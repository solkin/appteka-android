package com.tomclaw.appsend.screen.auth.request_code.di

import com.tomclaw.appsend.screen.auth.request_code.RequestCodeActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [RequestCodeModule::class])
interface RequestCodeComponent {

    fun inject(activity: RequestCodeActivity)

}
