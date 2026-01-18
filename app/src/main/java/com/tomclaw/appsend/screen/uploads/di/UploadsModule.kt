package com.tomclaw.appsend.screen.uploads.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.details.adapter.abi.AbiResourceProvider
import com.tomclaw.appsend.screen.details.adapter.abi.AbiResourceProviderImpl
import com.tomclaw.appsend.screen.uploads.AppConverter
import com.tomclaw.appsend.screen.uploads.AppConverterImpl
import com.tomclaw.appsend.screen.uploads.AppsResourceProvider
import com.tomclaw.appsend.screen.uploads.AppsResourceProviderImpl
import com.tomclaw.appsend.screen.uploads.UploadsInteractor
import com.tomclaw.appsend.screen.uploads.UploadsInteractorImpl
import com.tomclaw.appsend.screen.uploads.UploadsPresenter
import com.tomclaw.appsend.screen.uploads.UploadsPresenterImpl
import com.tomclaw.appsend.screen.uploads.adapter.app.AppItemBlueprint
import com.tomclaw.appsend.screen.uploads.adapter.app.AppItemPresenter
import com.tomclaw.appsend.util.PackageObserver
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.util.Locale

@Module
class UploadsModule(
    private val context: Context,
    private val userId: Int,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: UploadsInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        appConverter: AppConverter,
        schedulers: SchedulersFactory
    ): UploadsPresenter = UploadsPresenterImpl(
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
    ): UploadsInteractor = UploadsInteractorImpl(
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
        packageObserver: PackageObserver,
        abiResourceProvider: AbiResourceProvider
    ): AppConverter {
        return AppConverterImpl(resourceProvider, categoryConverter, packageObserver, abiResourceProvider)
    }

    @Provides
    @PerActivity
    internal fun provideAbiResourceProvider(): AbiResourceProvider {
        return AbiResourceProviderImpl(context.resources)
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
        presenter: UploadsPresenter,
        resourceProvider: AppsResourceProvider
    ) = AppItemPresenter(presenter, resourceProvider)

}
