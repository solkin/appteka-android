package com.tomclaw.appsend

import android.app.Application
import android.content.Context
import com.tomclaw.appsend.analytics.AnalyticsActivityCallback
import com.tomclaw.appsend.di.AppComponent
import com.tomclaw.appsend.di.AppModule
import com.tomclaw.appsend.di.DaggerAppComponent
import com.tomclaw.appsend.util.ApkIconLoader
import com.tomclaw.appsend.util.AppIconLoader
import com.tomclaw.appsend.util.SvgDecoder
import com.tomclaw.appsend.util.ThemeManager
import com.tomclaw.cache.DiskLruCache
import com.tomclaw.imageloader.SimpleImageLoader.initImageLoader
import com.tomclaw.imageloader.core.DiskCacheImpl
import com.tomclaw.imageloader.core.FileProviderImpl
import com.tomclaw.imageloader.core.MainExecutorImpl
import com.tomclaw.imageloader.core.MemoryCacheImpl
import com.tomclaw.imageloader.util.BitmapDecoder
import com.tomclaw.imageloader.util.loader.ContentLoader
import com.tomclaw.imageloader.util.loader.FileLoader
import com.tomclaw.imageloader.util.loader.UrlLoader
import java.util.concurrent.Executors

class Appteka : Application() {

    lateinit var component: AppComponent
        private set

    lateinit var themeManager: ThemeManager
        private set

    override fun onCreate() {
        super.onCreate()
        themeManager = ThemeManager(this)
        themeManager.init()

        component = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()

        initSimpleImageLoader()
        initAnalytics()
    }

    private fun initSimpleImageLoader() {
        try {
            initImageLoader(
                decoders = listOf(SvgDecoder(), BitmapDecoder()),
                fileProvider = FileProviderImpl(
                    cacheDir,
                    DiskCacheImpl(DiskLruCache.create(cacheDir, DISK_CACHE_SIZE)),
                    UrlLoader(),
                    FileLoader(assets),
                    ContentLoader(contentResolver),
                    AppIconLoader(this, packageManager),
                    ApkIconLoader(this, packageManager)
                ),
                memoryCache = MemoryCacheImpl(),
                mainExecutor = MainExecutorImpl(),
                backgroundExecutor = Executors.newFixedThreadPool(IMAGE_LOADER_THREADS)
            )
        } catch (ignored: Throwable) {
        }
    }

    private fun initAnalytics() {
        component.migrationManager()
        component.analytics().register()
        registerActivityLifecycleCallbacks(
            AnalyticsActivityCallback(component.bananalytics())
        )
    }

    companion object {
        private const val DISK_CACHE_SIZE = 15L * 1024 * 1024 // 15 MB
        private const val IMAGE_LOADER_THREADS = 5
    }
}

val Context.appComponent: AppComponent
    get() = (applicationContext as Appteka).component
