package com.tomclaw.appsend.screen.edit_profile.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.core.permissions.UserCapabilitiesProvider
import com.tomclaw.appsend.screen.edit_profile.EditProfileInteractor
import com.tomclaw.appsend.screen.edit_profile.EditProfileInteractorImpl
import com.tomclaw.appsend.screen.edit_profile.EditProfilePresenter
import com.tomclaw.appsend.screen.edit_profile.EditProfilePresenterImpl
import com.tomclaw.appsend.screen.edit_profile.EditProfileResourceProvider
import com.tomclaw.appsend.screen.edit_profile.EditProfileResourceProviderImpl
import com.tomclaw.appsend.util.ImageCompressor
import com.tomclaw.appsend.util.ImageCompressorImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides

@Module
class EditProfileModule(
    private val context: Context,
    private val state: Bundle?,
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: EditProfileInteractor,
        resourceProvider: EditProfileResourceProvider,
        capabilitiesProvider: UserCapabilitiesProvider,
        schedulers: SchedulersFactory,
    ): EditProfilePresenter = EditProfilePresenterImpl(
        interactor,
        resourceProvider,
        capabilitiesProvider,
        schedulers,
        state,
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        compressor: ImageCompressor,
        schedulers: SchedulersFactory,
    ): EditProfileInteractor = EditProfileInteractorImpl(api, compressor, schedulers)

    @Provides
    @PerActivity
    internal fun provideImageCompressor(): ImageCompressor =
        ImageCompressorImpl(context.contentResolver)

    @Provides
    @PerActivity
    internal fun provideResourceProvider(): EditProfileResourceProvider =
        EditProfileResourceProviderImpl(context.resources)

}
