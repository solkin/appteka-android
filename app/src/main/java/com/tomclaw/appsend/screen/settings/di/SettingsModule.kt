package com.tomclaw.appsend.screen.settings.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.di.APPS_DIR
import com.tomclaw.appsend.screen.settings.SettingsInteractor
import com.tomclaw.appsend.screen.settings.SettingsInteractorImpl
import com.tomclaw.appsend.screen.settings.SettingsPresenter
import com.tomclaw.appsend.screen.settings.SettingsPresenterImpl
import com.tomclaw.appsend.screen.settings.SettingsResourceProvider
import com.tomclaw.appsend.screen.settings.SettingsResourceProviderImpl
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.PerFragment
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Named

@Module
class SettingsModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerFragment
    internal fun providePresenter(
        settingsInteractor: SettingsInteractor,
        resourceProvider: SettingsResourceProvider,
        analytics: Analytics,
        schedulers: SchedulersFactory
    ): SettingsPresenter = SettingsPresenterImpl(
        settingsInteractor,
        resourceProvider,
        analytics,
        schedulers,
        state
    )

    @Provides
    @PerFragment
    internal fun provideResourceProvider(): SettingsResourceProvider =
        SettingsResourceProviderImpl(context.resources)

    @Provides
    @PerFragment
    internal fun provideInteractor(
        @Named(APPS_DIR) appsDir: File,
        resourceProvider: SettingsResourceProvider,
        schedulers: SchedulersFactory
    ): SettingsInteractor = SettingsInteractorImpl(context, appsDir, resourceProvider, schedulers)

}

