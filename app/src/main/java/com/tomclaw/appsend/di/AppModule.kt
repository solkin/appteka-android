package com.tomclaw.appsend.di

import android.app.Application
import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.tomclaw.appsend.core.Config
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.net.Session
import com.tomclaw.appsend.net.UserData
import com.tomclaw.appsend.util.Logger
import com.tomclaw.appsend.util.LoggerImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Named
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

    @Provides
    @Singleton
    internal fun provideLocale(): Locale = Locale.getDefault()

    @Provides
    @Singleton
    @Named(TIME_FORMATTER)
    internal fun provideTimeFormatter(locale: Locale): DateFormat =
        SimpleDateFormat("HH:mm", locale)

    @Provides
    @Singleton
    @Named(DATE_FORMATTER)
    internal fun provideDateFormatter(locale: Locale): DateFormat =
        SimpleDateFormat("dd.MM.yy", locale)

    @Provides
    @Singleton
    internal fun provideUserData(): UserData = Session.getInstance().userData

    @Provides
    @Singleton
    internal fun provideHttClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ChuckerInterceptor.Builder(app).build())
        .build()

    @Provides
    @Singleton
    internal fun provideStoreApi(client: OkHttpClient): StoreApi = Retrofit.Builder()
        .client(client)
        .baseUrl(Config.HOST_URL + "/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
        .create(StoreApi::class.java)

}

const val TIME_FORMATTER = "TimeFormatter"
const val DATE_FORMATTER = "DateFormatter"
