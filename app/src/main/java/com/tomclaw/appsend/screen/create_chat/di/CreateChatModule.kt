package com.tomclaw.appsend.screen.create_chat.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.create_chat.CreateChatInteractor
import com.tomclaw.appsend.screen.create_chat.CreateChatInteractorImpl
import com.tomclaw.appsend.screen.create_chat.CreateChatPresenter
import com.tomclaw.appsend.screen.create_chat.CreateChatPresenterImpl
import com.tomclaw.appsend.screen.create_chat.CreateChatResourceProvider
import com.tomclaw.appsend.screen.create_chat.CreateChatResourceProviderImpl
import com.tomclaw.appsend.util.ImageCompressor
import com.tomclaw.appsend.util.ImageCompressorImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides

@Module
class CreateChatModule(
    private val context: Context,
    private val state: Bundle?,
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: CreateChatInteractor,
        resourceProvider: CreateChatResourceProvider,
        schedulers: SchedulersFactory,
    ): CreateChatPresenter = CreateChatPresenterImpl(
        interactor,
        resourceProvider,
        schedulers,
        state,
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        compressor: ImageCompressor,
        schedulers: SchedulersFactory,
    ): CreateChatInteractor = CreateChatInteractorImpl(api, compressor, schedulers)

    @Provides
    @PerActivity
    internal fun provideImageCompressor(): ImageCompressor =
        ImageCompressorImpl(context.contentResolver)

    @Provides
    @PerActivity
    internal fun provideResourceProvider(): CreateChatResourceProvider =
        CreateChatResourceProviderImpl(context.resources)

}
