package com.tomclaw.appsend.screen.distro.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleAdapterPresenter
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.core.PackageInfoProvider
import com.tomclaw.appsend.core.StreamsProvider
import com.tomclaw.appsend.download.ApkStorage
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
import java.util.Locale

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
        infoProvider: DistroInfoProvider,
        apkStorage: ApkStorage,
        streamsProvider: StreamsProvider,
        schedulers: SchedulersFactory
    ): DistroInteractor = DistroInteractorImpl(
        infoProvider,
        apkStorage,
        streamsProvider,
        schedulers
    )

    @Provides
    @PerActivity
    internal fun provideResourceProvider(locale: Locale): DistroResourceProvider {
        return DistroResourceProviderImpl(context.resources, locale)
    }

    @Provides
    @PerActivity
    internal fun provideDistroInfoProvider(
        apkStorage: ApkStorage,
        packageInfoProvider: PackageInfoProvider,
    ): DistroInfoProvider {
        return DistroInfoProviderImpl(
            apkStorage = apkStorage,
            packageInfoProvider = packageInfoProvider
        )
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
