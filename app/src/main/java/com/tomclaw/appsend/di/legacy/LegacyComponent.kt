package com.tomclaw.appsend.di.legacy

import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [LegacyModule::class])
interface LegacyComponent {

    fun inject(injector: LegacyInjector)

}