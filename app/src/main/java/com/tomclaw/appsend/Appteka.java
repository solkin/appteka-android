package com.tomclaw.appsend;

import static java.util.Collections.singletonList;
import static com.tomclaw.appsend.util.ThemesKt.initTheme;

import java.util.Arrays;

import android.app.Application;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.tomclaw.appsend.di.AppComponent;
import com.tomclaw.appsend.di.AppModule;
import com.tomclaw.appsend.di.DaggerAppComponent;
import com.tomclaw.appsend.di.legacy.LegacyInjector;
import com.tomclaw.appsend.di.legacy.LegacyModule;
import com.tomclaw.appsend.util.ApkIconLoader;
import com.tomclaw.appsend.util.AppIconLoader;
import com.tomclaw.appsend.util.SvgDecoder;
import com.tomclaw.appsend.util.states.StateHolder;
import com.tomclaw.cache.DiskLruCache;
import com.tomclaw.imageloader.SimpleImageLoader;
import com.tomclaw.imageloader.core.DiskCacheImpl;
import com.tomclaw.imageloader.core.FileProvider;
import com.tomclaw.imageloader.core.FileProviderImpl;
import com.tomclaw.imageloader.core.MainExecutorImpl;
import com.tomclaw.imageloader.core.MemoryCacheImpl;
import com.tomclaw.imageloader.util.BitmapDecoder;
import com.tomclaw.imageloader.util.loader.ContentLoader;
import com.tomclaw.imageloader.util.loader.FileLoader;
import com.tomclaw.imageloader.util.loader.UrlLoader;

import java.io.IOException;
import java.util.concurrent.Executors;

public class Appteka extends Application {

    private static Appteka app;

    private static AppComponent component;

    private final LegacyInjector injector = new LegacyInjector();

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        component = buildComponent();

        initImageLoader();
        StateHolder.init();

        component.legacyComponent(new LegacyModule()).inject(injector);

        injector.getMigration();

        injector.analytics.register();

        // Initialize theme from ThemesKt
        initTheme(this);
    }

    public static AppComponent getComponent() {
        return component;
    }

    private AppComponent buildComponent() {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    private void initImageLoader() {
        try {
            FileProvider fileProvider = new FileProviderImpl(
                    getCacheDir(),
                    new DiskCacheImpl(DiskLruCache.create(getCacheDir(), 15728640L)),
                    new UrlLoader(),
                    new FileLoader(getAssets()),
                    new ContentLoader(getContentResolver()),
                    new AppIconLoader(getPackageManager()),
                    new ApkIconLoader(getPackageManager())
            );
            SimpleImageLoader.INSTANCE.initImageLoader(this, Arrays.asList(
                    new SvgDecoder(),
                    new BitmapDecoder()
            ), fileProvider, new MemoryCacheImpl(), new MainExecutorImpl(),
                    Executors.newFixedThreadPool(5));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Appteka app() {
        return app;
    }

}