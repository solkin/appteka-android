package com.tomclaw.appsend.screen.profile.di

import com.tomclaw.appsend.screen.profile.ProfileFragment
import com.tomclaw.appsend.util.PerFragment
import dagger.Subcomponent

@PerFragment
@Subcomponent(modules = [ProfileModule::class])
interface ProfileComponent {

    fun inject(fragment: ProfileFragment)

}