package com.tomclaw.appsend.screen.upload.di

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Bundle
import com.tomclaw.appsend.screen.upload.UploadInteractor
import com.tomclaw.appsend.screen.upload.UploadInteractorImpl
import com.tomclaw.appsend.screen.upload.UploadPresenter
import com.tomclaw.appsend.screen.upload.UploadPresenterImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides

@Module
class UploadModule(
    private val context: Context,
    private val info: PackageInfo?,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: UploadInteractor,
        schedulers: SchedulersFactory
    ): UploadPresenter = UploadPresenterImpl(info, interactor, schedulers, state)

    @Provides
    @PerActivity
    internal fun provideInteractor(
        schedulers: SchedulersFactory
    ): UploadInteractor = UploadInteractorImpl(schedulers)

}