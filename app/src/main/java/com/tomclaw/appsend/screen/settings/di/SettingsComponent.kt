/* package com.tomclaw.appsend.screen.settings.di

import com.tomclaw.appsend.screen.settings.SettingsActivity
import com.tomclaw.appsend.screen.settings.SettingsFragment
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.PerFragment
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [SettingsActivityModule::class])
interface SettingsActivityComponent {

    fun inject(activity: SettingsActivity)

}

@PerFragment
@Subcomponent(modules = [SettingsModule::class])
interface SettingsComponent {

    fun inject(fragment: SettingsFragment)

}

*/