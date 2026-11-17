package com.tomclaw.appsend.screen.search.di

import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.tomclaw.appsend.dto.DeviceInfo
import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleAdapterPresenter
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.di.USER_DIR
import com.tomclaw.appsend.screen.search.SearchHistoryStorage
import com.tomclaw.appsend.screen.search.SearchHistoryStorageImpl
import com.tomclaw.appsend.screen.search.SearchInteractor
import com.tomclaw.appsend.screen.search.SearchInteractorImpl
import com.tomclaw.appsend.screen.search.SearchPresenter
import com.tomclaw.appsend.screen.search.SearchPresenterImpl
import com.tomclaw.appsend.screen.details.adapter.abi.AbiResourceProvider
import com.tomclaw.appsend.screen.details.adapter.abi.AbiResourceProviderImpl
import com.tomclaw.appsend.screen.store.AppConverter
import com.tomclaw.appsend.screen.store.AppConverterImpl
import com.tomclaw.appsend.screen.store.AppsResourceProvider
import com.tomclaw.appsend.screen.store.AppsResourceProviderImpl
import com.tomclaw.appsend.screen.store.StorePreferencesProvider
import com.tomclaw.appsend.screen.store.StorePreferencesProviderImpl
import com.tomclaw.appsend.screen.store.adapter.app.AppItemBlueprint
import com.tomclaw.appsend.screen.store.adapter.app.AppItemPresenter
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.PackageObserver
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.io.File
import java.util.Locale
import javax.inject.Named

@Module
class SearchModule(
    private val context: Context,
    /** Tags the screen opens with, e.g. one tapped on an app page. */
    private val initialTags: List<String>,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        searchInteractor: SearchInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        appConverter: AppConverter,
        analytics: Analytics,
        schedulers: SchedulersFactory
    ): SearchPresenter = SearchPresenterImpl(
        searchInteractor,
        adapterPresenter,
        appConverter,
        analytics,
        schedulers,
        initialTags,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        historyStorage: SearchHistoryStorage,
        locale: Locale,
        schedulers: SchedulersFactory
    ): SearchInteractor = SearchInteractorImpl(api, historyStorage, locale, schedulers)

    @Provides
    @PerActivity
    internal fun provideHistoryStorage(
        @Named(USER_DIR) filesDir: File,
        gson: Gson
    ): SearchHistoryStorage = SearchHistoryStorageImpl(filesDir, gson)

    @Provides
    @PerActivity
    internal fun provideResourceProvider(): AppsResourceProvider {
        return AppsResourceProviderImpl(context.resources)
    }

    @Provides
    @PerActivity
    internal fun provideStorePreferencesProvider(): StorePreferencesProvider {
        return StorePreferencesProviderImpl(context)
    }

    @Provides
    @PerActivity
    internal fun provideAppsConverter(
        resourceProvider: AppsResourceProvider,
        categoryConverter: CategoryConverter,
        packageObserver: PackageObserver,
        abiResourceProvider: AbiResourceProvider,
    ): AppConverter {
        return AppConverterImpl(resourceProvider, categoryConverter, packageObserver, abiResourceProvider)
    }

    @Provides
    @PerActivity
    internal fun provideAbiResourceProvider(device: DeviceInfo): AbiResourceProvider {
        return AbiResourceProviderImpl(context.resources, device)
    }

    @Provides
    @PerActivity
    internal fun provideAdapterPresenter(binder: ItemBinder): AdapterPresenter {
        return SimpleAdapterPresenter(binder)
    }

    @Provides
    @PerActivity
    internal fun provideItemBinder(
        blueprintSet: Set<@JvmSuppressWildcards ItemBlueprint<*, *>>
    ): ItemBinder {
        return ItemBinder.Builder().apply {
            blueprintSet.forEach { registerItem(it) }
        }.build()
    }

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideAppItemBlueprint(
        presenter: AppItemPresenter
    ): ItemBlueprint<*, *> = AppItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideAppItemPresenter(
        presenter: SearchPresenter,
        resourceProvider: AppsResourceProvider
    ) = AppItemPresenter(presenter, resourceProvider)

    @Provides
    @PerActivity
    internal fun provideCategoryConverter(locale: Locale): CategoryConverter =
        CategoryConverterImpl(locale)

}

