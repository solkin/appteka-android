package com.tomclaw.appsend.screen.agreement.di

import com.tomclaw.appsend.screen.agreement.AgreementActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [AgreementModule::class])
interface AgreementComponent {

    fun inject(activity: AgreementActivity)

}
