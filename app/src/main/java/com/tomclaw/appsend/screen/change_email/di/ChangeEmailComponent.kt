package com.tomclaw.appsend.screen.change_email.di

import com.tomclaw.appsend.screen.change_email.ChangeEmailActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [ChangeEmailModule::class])
interface ChangeEmailComponent {

    fun inject(activity: ChangeEmailActivity)

}
