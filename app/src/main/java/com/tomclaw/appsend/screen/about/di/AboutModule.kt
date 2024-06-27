package com.tomclaw.appsend.screen.about.di

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import com.tomclaw.appsend.core.AppInfoProvider
import com.tomclaw.appsend.core.UserAgentProvider
import com.tomclaw.appsend.screen.about.AboutPresenter
import com.tomclaw.appsend.screen.about.AboutPresenterImpl
import com.tomclaw.appsend.screen.about.AboutResourceProvider
import com.tomclaw.appsend.screen.about.AboutResourceProviderImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides
import java.util.Locale

@Module
class AboutModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        resourceProvider: AboutResourceProvider,
        userAgentProvider: UserAgentProvider,
        schedulers: SchedulersFactory
    ): AboutPresenter = AboutPresenterImpl(
        resourceProvider,
        userAgentProvider,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideResourceProvider(
        infoProvider: AppInfoProvider,
    ): AboutResourceProvider = AboutResourceProviderImpl(
        infoProvider,
        context.resources,
    )

}
