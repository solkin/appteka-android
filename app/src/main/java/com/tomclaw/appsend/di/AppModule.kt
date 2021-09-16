package com.tomclaw.appsend.di

import android.app.Application
import android.content.Context
import com.tomclaw.appsend.util.Logger
import com.tomclaw.appsend.util.LoggerImpl
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton

@Module
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    internal fun provideContext(): Context = app

    @Provides
    @Singleton
    internal fun provideSchedulersFactory(): SchedulersFactory =
        SchedulersFactory.SchedulersFactoryImpl()

    @Provides
    @Singleton
    fun provideFilesDir(): File = app.filesDir

    @Provides
    @Singleton
    internal fun provideLogger(): Logger = LoggerImpl()

}
