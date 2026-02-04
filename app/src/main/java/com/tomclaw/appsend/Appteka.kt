package com.tomclaw.appsend

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.tomclaw.appsend.analytics.AnalyticsActivityCallback
import com.tomclaw.appsend.di.AppComponent
import com.tomclaw.appsend.di.AppModule
import com.tomclaw.appsend.di.DaggerAppComponent
import com.tomclaw.appsend.util.ApkIconLoader
import com.tomclaw.appsend.util.AppIconLoader
import com.tomclaw.appsend.util.SvgDecoder
import com.tomclaw.appsend.util.initTheme
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
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.concurrent.Executors

class Appteka : Application() {

    lateinit var component: AppComponent
        private set

    private val activityList = ArrayList<WeakReference<Activity>>()

    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        applyAppThemeFast()

        registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacks {
                override fun onActivityPreCreated(activity: Activity, savedInstanceState: android.os.Bundle?) {
                    applyDynamicOrCustomTheme(activity)
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) {
                    activityList.add(WeakReference(activity))
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        applyDynamicOrCustomTheme(activity)
                    }
                }

                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {
                    val iterator = activityList.iterator()
                    while (iterator.hasNext()) {
                        val ref = iterator.next()
                        if (ref.get() == null || ref.get() == activity) {
                            iterator.remove()
                        }
                    }
                }
            }
        )

        initSimpleImageLoader()
        initAnalytics()
    }

    private fun applyAppThemeFast() {
        val modeKey = getString(R.string.pref_theme_mode)
        val defaultMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        } else {
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }

        var mode = prefs.getInt(modeKey, defaultMode)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            mode = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
        AppCompatDelegate.setDefaultNightMode(mode)

        val dynamicKey = getString(R.string.pref_dynamic_colors)
        val dynamicEnabled = prefs.getBoolean(dynamicKey, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

        if (dynamicEnabled) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        } else {
            val seedColor = prefs.getInt(KEY_SEED_COLOR, DEFAULT_SEED_COLOR)
            val options = DynamicColorsOptions.Builder()
                .setContentBasedSource(seedColor)
                .build()
            DynamicColors.applyToActivitiesIfAvailable(this, options)
        }
    }

    private fun applyDynamicOrCustomTheme(activity: Activity) {
        val dynamicKey = getString(R.string.pref_dynamic_colors)
        val dynamicEnabled = prefs.getBoolean(dynamicKey, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

        if (dynamicEnabled) {
            DynamicColors.applyToActivityIfAvailable(activity)
        } else {
            val seedColor = prefs.getInt(KEY_SEED_COLOR, DEFAULT_SEED_COLOR)
            val options = DynamicColorsOptions.Builder()
                .setContentBasedSource(seedColor)
                .build()
            DynamicColors.applyToActivityIfAvailable(activity, options)
        }
    }

    fun isDynamicColorsEnabled(): Boolean {
        val dynamicKey = getString(R.string.pref_dynamic_colors)
        return prefs.getBoolean(dynamicKey, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    }

    fun setDynamicColorsEnabled(enabled: Boolean) {
        val dynamicKey = getString(R.string.pref_dynamic_colors)
        prefs.edit().putBoolean(dynamicKey, enabled).apply()

        // Apply immediately
        if (enabled) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        } else {
            val seedColor = prefs.getInt(KEY_SEED_COLOR, DEFAULT_SEED_COLOR)
            val options = DynamicColorsOptions.Builder()
                .setContentBasedSource(seedColor)
                .build()
            DynamicColors.applyToActivitiesIfAvailable(this, options)
        }

        // Recreate all activities for full update
        recreateAllActivities()
    }

    fun getSeedColor(): Int {
        return prefs.getInt(KEY_SEED_COLOR, DEFAULT_SEED_COLOR)
    }

    fun setSeedColor(color: Int) {
        prefs.edit().putInt(KEY_SEED_COLOR, color).apply()

        // Apply seed color app-wide
        if (!isDynamicColorsEnabled()) {
            val options = DynamicColorsOptions.Builder()
                .setContentBasedSource(color)
                .build()
            DynamicColors.applyToActivitiesIfAvailable(this, options)
        }

        // Recreate all activities for immediate update
        recreateAllActivities()
    }

    fun recreateAllActivities() {
        for (ref in activityList) {
            val activity = ref.get()
            if (activity != null && !activity.isFinishing) {
                activity.recreate()
            }
        }
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

        private const val KEY_SEED_COLOR = "seed_color"
        const val DEFAULT_SEED_COLOR = -0xCD5CFC  // #FF32A304
    }
}

val Context.appComponent: AppComponent
    get() = (applicationContext as Appteka).component