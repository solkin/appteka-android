package com.tomclaw.appsend.screen.installed.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleAdapterPresenter
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.core.StreamsProvider
import com.tomclaw.appsend.download.ApkStorage
import com.tomclaw.appsend.screen.installed.AppConverter
import com.tomclaw.appsend.screen.installed.AppConverterImpl
import com.tomclaw.appsend.screen.installed.AppsResourceProvider
import com.tomclaw.appsend.screen.installed.AppsResourceProviderImpl
import com.tomclaw.appsend.screen.installed.InstalledInfoProvider
import com.tomclaw.appsend.screen.installed.InstalledInfoProviderImpl
import com.tomclaw.appsend.screen.installed.InstalledInteractor
import com.tomclaw.appsend.screen.installed.InstalledInteractorImpl
import com.tomclaw.appsend.screen.installed.InstalledPreferencesProvider
import com.tomclaw.appsend.screen.installed.InstalledPreferencesProviderImpl
import com.tomclaw.appsend.screen.installed.InstalledPresenter
import com.tomclaw.appsend.screen.installed.InstalledPresenterImpl
import com.tomclaw.appsend.screen.installed.adapter.app.AppItemBlueprint
import com.tomclaw.appsend.screen.installed.adapter.app.AppItemPresenter
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.util.Locale

@Module
class InstalledModule(
    private val context: Context,
    private val picker: Boolean,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        preferencesProvider: InstalledPreferencesProvider,
        interactor: InstalledInteractor,
        apkStorage: ApkStorage,
        adapterPresenter: Lazy<AdapterPresenter>,
        appConverter: AppConverter,
        schedulers: SchedulersFactory
    ): InstalledPresenter = InstalledPresenterImpl(
        picker,
        preferencesProvider,
        interactor,
        apkStorage,
        adapterPresenter,
        appConverter,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        locale: Locale,
        apkStorage: ApkStorage,
        streamsProvider: StreamsProvider,
        infoProvider: InstalledInfoProvider,
        schedulers: SchedulersFactory
    ): InstalledInteractor = InstalledInteractorImpl(
        api,
        locale,
        apkStorage,
        streamsProvider,
        infoProvider,
        schedulers
    )

    @Provides
    @PerActivity
    internal fun provideResourceProvider(locale: Locale): AppsResourceProvider {
        return AppsResourceProviderImpl(context.resources, locale)
    }

    @Provides
    @PerActivity
    internal fun provideInstalledInfoProvider(): InstalledInfoProvider {
        return InstalledInfoProviderImpl(context.packageManager)
    }

    @Provides
    @PerActivity
    internal fun provideInstalledPreferencesProvider(): InstalledPreferencesProvider {
        return InstalledPreferencesProviderImpl(context)
    }

    @Provides
    @PerActivity
    internal fun provideAppsConverter(): AppConverter = AppConverterImpl()

    @Provides
    @PerActivity
    internal fun provideCategoryConverter(locale: Locale): CategoryConverter =
        CategoryConverterImpl(locale)

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
        presenter: InstalledPresenter,
        resourceProvider: AppsResourceProvider
    ) = AppItemPresenter(presenter, resourceProvider)

}
