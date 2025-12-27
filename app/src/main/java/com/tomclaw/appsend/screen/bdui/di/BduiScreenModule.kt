package com.tomclaw.appsend.screen.bdui.di

import android.os.Bundle
import com.tomclaw.appsend.screen.bdui.BduiScreenInteractor
import com.tomclaw.appsend.screen.bdui.BduiScreenInteractorImpl
import com.tomclaw.appsend.screen.bdui.BduiScreenPresenter
import com.tomclaw.appsend.screen.bdui.BduiScreenPresenterImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient

@Module
class BduiScreenModule(
    private val url: String,
    private val title: String?,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: BduiScreenInteractor,
        schedulers: SchedulersFactory
    ): BduiScreenPresenter = BduiScreenPresenterImpl(
        url = url,
        title = title,
        interactor = interactor,
        schedulers = schedulers,
        state = state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        httpClient: OkHttpClient,
        schedulers: SchedulersFactory
    ): BduiScreenInteractor = BduiScreenInteractorImpl(
        httpClient = httpClient,
        schedulers = schedulers
    )

}
