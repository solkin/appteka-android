package com.tomclaw.appsend.screen.distro.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.di.APPS_DIR
import com.tomclaw.appsend.screen.distro.ApkConverter
import com.tomclaw.appsend.screen.distro.ApkConverterImpl
import com.tomclaw.appsend.screen.distro.DistroInfoProvider
import com.tomclaw.appsend.screen.distro.DistroInfoProviderImpl
import com.tomclaw.appsend.screen.distro.DistroInteractor
import com.tomclaw.appsend.screen.distro.DistroInteractorImpl
import com.tomclaw.appsend.screen.distro.DistroPreferencesProvider
import com.tomclaw.appsend.screen.distro.DistroPreferencesProviderImpl
import com.tomclaw.appsend.screen.distro.DistroPresenter
import com.tomclaw.appsend.screen.distro.DistroPresenterImpl
import com.tomclaw.appsend.screen.distro.DistroResourceProvider
import com.tomclaw.appsend.screen.distro.DistroResourceProviderImpl
import com.tomclaw.appsend.screen.distro.adapter.apk.ApkItemBlueprint
import com.tomclaw.appsend.screen.distro.adapter.apk.ApkItemPresenter
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
class DistroModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        preferencesProvider: DistroPreferencesProvider,
        interactor: DistroInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        appConverter: ApkConverter,
        schedulers: SchedulersFactory
    ): DistroPresenter = DistroPresenterImpl(
        preferencesProvider,
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
        @Named(APPS_DIR) appsDir: File,
        locale: Locale,
        infoProvider: DistroInfoProvider,
        schedulers: SchedulersFactory
    ): DistroInteractor = DistroInteractorImpl(
        api,
        appsDir,
        locale,
        infoProvider,
        schedulers
    )

    @Provides
    @PerActivity
    internal fun provideResourceProvider(locale: Locale): DistroResourceProvider {
        return DistroResourceProviderImpl(context.resources, locale)
    }

    @Provides
    @PerActivity
    internal fun provideDistroInfoProvider(): DistroInfoProvider {
        return DistroInfoProviderImpl(context.packageManager)
    }

    @Provides
    @PerActivity
    internal fun provideDistroPreferencesProvider(): DistroPreferencesProvider {
        return DistroPreferencesProviderImpl(context)
    }

    @Provides
    @PerActivity
    internal fun provideApkConverter(): ApkConverter = ApkConverterImpl()

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
    internal fun provideApkItemBlueprint(
        presenter: ApkItemPresenter
    ): ItemBlueprint<*, *> = ApkItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideApkItemPresenter(
        presenter: DistroPresenter,
        resourceProvider: DistroResourceProvider
    ) = ApkItemPresenter(presenter, resourceProvider)

}
