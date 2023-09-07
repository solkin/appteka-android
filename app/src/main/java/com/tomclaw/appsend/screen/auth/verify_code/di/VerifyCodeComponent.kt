package com.tomclaw.appsend.screen.auth.verify_code.di

import com.tomclaw.appsend.screen.auth.verify_code.VerifyCodeActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [VerifyCodeModule::class])
interface VerifyCodeComponent {

    fun inject(activity: VerifyCodeActivity)

}
