package com.tomclaw.appsend.di

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tomclaw.appsend.core.Config
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.events.EventsInteractor
import com.tomclaw.appsend.events.EventsInteractorImpl
import com.tomclaw.appsend.user.SessionStorage
import com.tomclaw.appsend.user.SessionStorageImpl
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.user.UserDataInteractorImpl
import com.tomclaw.appsend.util.Logger
import com.tomclaw.appsend.util.LoggerImpl
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

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
    internal fun provideUserDataInteractor(
        sessionStorage: SessionStorage,
        api: StoreApi,
        schedulers: SchedulersFactory
    ): UserDataInteractor = UserDataInteractorImpl(sessionStorage, api, schedulers)

    @Provides
    @Singleton
    internal fun provideSessionInteractor(
        gson: Gson,
        schedulers: SchedulersFactory
    ): SessionStorage = SessionStorageImpl(app.getDir(USER_DIR, MODE_PRIVATE), gson, schedulers)

    @Provides
    @Singleton
    internal fun provideEventsInteractor(
        userDataInteractor: UserDataInteractor,
        api: StoreApi,
        schedulers: SchedulersFactory
    ): EventsInteractor = EventsInteractorImpl(userDataInteractor, api, schedulers)

    @Provides
    @Singleton
    internal fun provideGson(): Gson = GsonBuilder().create()

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
const val USER_DIR = "user"
