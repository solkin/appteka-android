package com.tomclaw.appsend.screen.settings.di

import android.content.Context
import com.tomclaw.appsend.util.PerActivity
import dagger.Module
import dagger.Provides

@Module
class SettingsActivityModule(
    private val context: Context
) {

    @Provides
    @PerActivity
    internal fun provideContext(): Context = context

}

