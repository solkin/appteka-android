package com.tomclaw.appsend.screen.avatar_crop.di

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.tomclaw.appsend.di.PICKED_MEDIA_CACHE
import com.tomclaw.appsend.screen.avatar_crop.AvatarCropInteractor
import com.tomclaw.appsend.screen.avatar_crop.AvatarCropInteractorImpl
import com.tomclaw.appsend.screen.avatar_crop.AvatarCropPresenter
import com.tomclaw.appsend.screen.avatar_crop.AvatarCropPresenterImpl
import com.tomclaw.appsend.screen.avatar_crop.AvatarCropResourceProvider
import com.tomclaw.appsend.screen.avatar_crop.AvatarCropResourceProviderImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.cache.DiskLruCache
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class AvatarCropModule(
    private val context: Context,
    private val sourceUri: Uri,
    private val cacheKey: String,
    private val state: Bundle?,
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: AvatarCropInteractor,
        resourceProvider: AvatarCropResourceProvider,
        schedulers: SchedulersFactory,
    ): AvatarCropPresenter = AvatarCropPresenterImpl(
        sourceUri,
        cacheKey,
        interactor,
        resourceProvider,
        schedulers,
        state,
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        @Named(PICKED_MEDIA_CACHE) pickedMediaCache: DiskLruCache,
        schedulers: SchedulersFactory,
    ): AvatarCropInteractor = AvatarCropInteractorImpl(
        context = context,
        contentResolver = context.contentResolver,
        pickedMediaCache = pickedMediaCache,
        schedulers = schedulers,
    )

    @Provides
    @PerActivity
    internal fun provideResourceProvider(): AvatarCropResourceProvider =
        AvatarCropResourceProviderImpl(context.resources)

}
