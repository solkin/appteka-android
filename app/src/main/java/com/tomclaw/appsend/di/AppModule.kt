package com.tomclaw.appsend.di

import android.app.Application
import com.tomclaw.appsend.BuildConfig
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tomclaw.appsend.analytics.EnvironmentProviderImpl
import com.tomclaw.bananalytics.Bananalytics
import com.tomclaw.bananalytics.BananalyticsConfig
import com.tomclaw.bananalytics.BananalyticsImpl
import com.tomclaw.bananalytics.EnvironmentProvider
import com.tomclaw.appsend.categories.CategoriesInteractor
import com.tomclaw.appsend.categories.CategoriesInteractorImpl
import com.tomclaw.appsend.core.AppInfoProvider
import com.tomclaw.appsend.core.AppInfoProviderImpl
import com.tomclaw.appsend.core.AppInfoInterceptor
import com.tomclaw.appsend.core.BANANALYTICS_API_KEY
import com.tomclaw.appsend.core.BANANALYTICS_URL
import com.tomclaw.appsend.core.DeviceIdInterceptor
import com.tomclaw.appsend.core.DeviceIdProvider
import com.tomclaw.appsend.core.DeviceIdProviderImpl
import com.tomclaw.appsend.core.HOST_URL
import com.tomclaw.appsend.core.MigrationManager
import com.tomclaw.appsend.core.MigrationManagerImpl
import com.tomclaw.appsend.core.PackageInfoProvider
import com.tomclaw.appsend.core.PackageInfoProviderImpl
import com.tomclaw.appsend.core.PersistentCookieJar
import com.tomclaw.appsend.core.STAND_BY_HOST_URL
import com.tomclaw.appsend.core.StandByApi
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.core.StreamsProvider
import com.tomclaw.appsend.core.StreamsProviderImpl
import com.tomclaw.appsend.core.TimeProvider
import com.tomclaw.appsend.core.TimeProviderImpl
import com.tomclaw.appsend.core.UserAgentInterceptor
import com.tomclaw.appsend.core.UserAgentProvider
import com.tomclaw.appsend.core.UserAgentProviderImpl
import com.tomclaw.appsend.download.ApkStorage
import com.tomclaw.appsend.download.DownloadManager
import com.tomclaw.appsend.download.DownloadManagerImpl
import com.tomclaw.appsend.download.DownloadNotifications
import com.tomclaw.appsend.download.DownloadNotificationsImpl
import com.tomclaw.appsend.download.LegacyApkStorage
import com.tomclaw.appsend.download.MediaStoreApkStorage
import android.os.Build
import com.tomclaw.appsend.screen.details.DetailsDeepLinkParser
import com.tomclaw.appsend.screen.details.DetailsDeepLinkParserImpl
import com.tomclaw.appsend.screen.profile.ProfileDeepLinkParser
import com.tomclaw.appsend.screen.profile.ProfileDeepLinkParserImpl
import com.tomclaw.appsend.screen.feed.api.PostDeserializer
import com.tomclaw.appsend.screen.feed.api.PostEntity
import com.tomclaw.appsend.upload.UploadManager
import com.tomclaw.appsend.upload.UploadManagerImpl
import com.tomclaw.appsend.upload.UploadNotifications
import com.tomclaw.appsend.upload.UploadNotificationsImpl
import com.tomclaw.appsend.user.ModerationProvider
import com.tomclaw.appsend.user.ModerationProviderImpl
import com.tomclaw.appsend.user.SessionStorage
import com.tomclaw.appsend.user.SessionStorageImpl
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.AnalyticsImpl
import com.tomclaw.appsend.util.Logger
import com.tomclaw.appsend.util.LoggerImpl
import com.tomclaw.appsend.util.PackageObserver
import com.tomclaw.appsend.util.PackageObserverImpl
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.SchedulersFactoryImpl
import dagger.Module
import dagger.Provides
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    internal fun provideContext(): Context = app

    @Provides
    @Singleton
    internal fun provideSchedulersFactory(): SchedulersFactory = SchedulersFactoryImpl()

    @Provides
    @Singleton
    @Named(USER_DIR)
    fun provideFilesDir(): File = app.filesDir

    @Provides
    @Singleton
    internal fun provideApkStorage(context: Context): ApkStorage {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStoreApkStorage(context)
        } else {
            LegacyApkStorage(context)
        }
    }

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
    internal fun provideTimeProvider(context: Context): TimeProvider = TimeProviderImpl(context)

    @Provides
    @Singleton
    internal fun provideUserAgentProvider(
        appInfoProvider: AppInfoProvider,
        locale: Locale
    ): UserAgentProvider = UserAgentProviderImpl(appInfoProvider, locale)

    @Provides
    @Singleton
    internal fun provideDeviceIdProvider(
        @Named(USER_DIR) filesDir: File
    ): DeviceIdProvider =
        DeviceIdProviderImpl(app, filesDir)

    @Provides
    @Singleton
    internal fun provideAppInfoProvider(
        packageManager: PackageManager,
        locale: Locale
    ): AppInfoProvider = AppInfoProviderImpl(app, packageManager, locale)

    @Provides
    @Singleton
    internal fun provideMigrationManager(appInfoProvider: AppInfoProvider): MigrationManager =
        MigrationManagerImpl(app, appInfoProvider)

    @Provides
    @Singleton
    internal fun provideEnvironmentProvider(
        locale: Locale,
        appInfoProvider: AppInfoProvider,
        deviceIdProvider: DeviceIdProvider,
    ): EnvironmentProvider = EnvironmentProviderImpl(locale, appInfoProvider, deviceIdProvider)

    @Provides
    @Singleton
    internal fun provideBananalyticsConfig(): BananalyticsConfig = BananalyticsConfig(
        baseUrl = BANANALYTICS_URL,
        apiKey = BANANALYTICS_API_KEY,
    )

    @Provides
    @Singleton
    internal fun provideBananalytics(
        @Named(USER_DIR) filesDir: File,
        config: BananalyticsConfig,
        environmentProvider: EnvironmentProvider,
    ): Bananalytics = BananalyticsImpl(
        filesDir = filesDir,
        config = config,
        environmentProvider = environmentProvider,
        isDebug = BuildConfig.DEBUG
    )

    @Provides
    @Singleton
    internal fun provideDownloadNotifications(): DownloadNotifications =
        DownloadNotificationsImpl(app)

    @Provides
    @Singleton
    internal fun provideUploadNotifications(): UploadNotifications =
        UploadNotificationsImpl(app)

    @Provides
    @Singleton
    internal fun provideSessionInteractor(
        gson: Gson,
        schedulers: SchedulersFactory
    ): SessionStorage = SessionStorageImpl(app.getDir(USER_DIR, MODE_PRIVATE), gson, schedulers)

    @Provides
    @Singleton
    internal fun provideModerationProvider(): ModerationProvider = ModerationProviderImpl()

    @Provides
    @Singleton
    internal fun provideCategoriesInteractor(
        api: StoreApi,
        schedulers: SchedulersFactory
    ): CategoriesInteractor = CategoriesInteractorImpl(api, schedulers)

    @Provides
    @Singleton
    internal fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    internal fun provideAnalytics(
        bananalytics: Bananalytics
    ): Analytics = AnalyticsImpl(bananalytics)

    @Provides
    @Singleton
    internal fun providePackageObserver(
        context: Context
    ): PackageObserver = PackageObserverImpl(context, context.packageManager)

    @Provides
    @Singleton
    internal fun provideManager(
        context: Context
    ): PackageManager = context.packageManager

    @Provides
    @Singleton
    internal fun provideDownloadManager(
        apkStorage: ApkStorage,
        cookieJar: CookieJar,
    ): DownloadManager = DownloadManagerImpl(apkStorage, cookieJar)

    @Provides
    @Singleton
    internal fun provideUploadManager(
        context: Context,
        cookieJar: CookieJar,
        api: StoreApi,
        gson: Gson
    ): UploadManager = UploadManagerImpl(context, cookieJar, api, gson)

    @Provides
    @Singleton
    internal fun provideCookieJar(
        @Named(USER_DIR) filesDir: File,
    ): CookieJar = PersistentCookieJar(filesDir)

    @Provides
    @Singleton
    internal fun provideHttClient(
        cookieJar: CookieJar,
        userAgentProvider: UserAgentProvider,
        deviceIdProvider: DeviceIdProvider,
        appInfoProvider: AppInfoProvider,
    ): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(2, TimeUnit.MINUTES)
        .connectTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(UserAgentInterceptor(userAgentProvider.getUserAgent()))
        .addInterceptor(DeviceIdInterceptor(deviceIdProvider.getDeviceId()))
        .addInterceptor(AppInfoInterceptor(appInfoProvider))
        .addInterceptor(ChuckerInterceptor.Builder(app).build())
        .cookieJar(cookieJar)
        .build()

    @Provides
    @Singleton
    internal fun provideStoreApi(client: OkHttpClient, gson: Gson): StoreApi = Retrofit.Builder()
        .client(client)
        .baseUrl("$HOST_URL/api/")
        .addConverterFactory(
            GsonConverterFactory.create(
                gson.newBuilder()
                    .registerTypeAdapter(PostEntity::class.java, PostDeserializer(gson))
                    .create()
            )
        )
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
        .create(StoreApi::class.java)

    @Provides
    @Singleton
    internal fun provideStandByApi(client: OkHttpClient): StandByApi = Retrofit.Builder()
        .client(client)
        .baseUrl("$STAND_BY_HOST_URL/api/appteka/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
        .create(StandByApi::class.java)

    @Provides
    @Singleton
    internal fun provideStreamsProvider(client: OkHttpClient): StreamsProvider =
        StreamsProviderImpl(context = app, client)

    @Provides
    @Singleton
    internal fun providePackageInfoProvider(packageManager: PackageManager): PackageInfoProvider =
        PackageInfoProviderImpl(packageManager)

    @Provides
    @Singleton
    internal fun provideDetailsDeepLinkParser(): DetailsDeepLinkParser = DetailsDeepLinkParserImpl()

    @Provides
    @Singleton
    internal fun provideProfileDeepLinkParser(): ProfileDeepLinkParser = ProfileDeepLinkParserImpl()

}

const val TIME_FORMATTER = "TimeFormatter"
const val DATE_FORMATTER = "DateFormatter"
const val USER_DIR = "user"
