package com.tomclaw.appsend.screen.search.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.search.SearchInteractor
import com.tomclaw.appsend.screen.search.SearchInteractorImpl
import com.tomclaw.appsend.screen.search.SearchPresenter
import com.tomclaw.appsend.screen.search.SearchPresenterImpl
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
import java.util.Locale

@Module
class SearchModule(
    private val context: Context,
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
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        locale: Locale,
        schedulers: SchedulersFactory
    ): SearchInteractor = SearchInteractorImpl(api, locale, schedulers)

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
    ): AppConverter {
        return AppConverterImpl(resourceProvider, categoryConverter, packageObserver)
    }

    @Provides
    @PerActivity
    internal fun provideAdapterPresenter(binder: ItemBinder): AdapterPresenter {
        return SimpleAdapterPresenter(binder, binder)
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

