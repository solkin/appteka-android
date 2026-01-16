package com.tomclaw.appsend.screen.downloads.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.downloads.AppConverter
import com.tomclaw.appsend.screen.downloads.AppConverterImpl
import com.tomclaw.appsend.screen.downloads.AppsResourceProvider
import com.tomclaw.appsend.screen.downloads.AppsResourceProviderImpl
import com.tomclaw.appsend.screen.downloads.DownloadsInteractor
import com.tomclaw.appsend.screen.downloads.DownloadsInteractorImpl
import com.tomclaw.appsend.screen.downloads.DownloadsPresenter
import com.tomclaw.appsend.screen.downloads.DownloadsPresenterImpl
import com.tomclaw.appsend.screen.downloads.adapter.app.AppItemBlueprint
import com.tomclaw.appsend.screen.downloads.adapter.app.AppItemPresenter
import com.tomclaw.appsend.util.PackageObserver
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.util.Locale

@Module
class DownloadsModule(
    private val context: Context,
    private val userId: Int,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: DownloadsInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        appConverter: AppConverter,
        schedulers: SchedulersFactory
    ): DownloadsPresenter = DownloadsPresenterImpl(
        userId,
        interactor,
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
        schedulers: SchedulersFactory
    ): DownloadsInteractor = DownloadsInteractorImpl(
        api,
        userId,
        locale,
        schedulers
    )

    @Provides
    @PerActivity
    internal fun provideResourceProvider(): AppsResourceProvider {
        return AppsResourceProviderImpl(context.resources)
    }

    @Provides
    @PerActivity
    internal fun provideAppsConverter(
        resourceProvider: AppsResourceProvider,
        categoryConverter: CategoryConverter,
        packageObserver: PackageObserver
    ): AppConverter {
        return AppConverterImpl(resourceProvider, categoryConverter, packageObserver)
    }

    @Provides
    @PerActivity
    internal fun provideCategoryConverter(locale: Locale): CategoryConverter =
        CategoryConverterImpl(locale)

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
        presenter: DownloadsPresenter,
        resourceProvider: AppsResourceProvider
    ) = AppItemPresenter(presenter, resourceProvider)

}
