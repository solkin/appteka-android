package com.tomclaw.appsend.screen.store.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.store.AppConverter
import com.tomclaw.appsend.screen.store.AppConverterImpl
import com.tomclaw.appsend.screen.store.AppsResourceProvider
import com.tomclaw.appsend.screen.store.AppsResourceProviderImpl
import com.tomclaw.appsend.screen.store.StoreInteractor
import com.tomclaw.appsend.screen.store.StoreInteractorImpl
import com.tomclaw.appsend.screen.store.StorePresenter
import com.tomclaw.appsend.screen.store.StorePresenterImpl
import com.tomclaw.appsend.screen.store.adapter.app.AppItemBlueprint
import com.tomclaw.appsend.screen.store.adapter.app.AppItemPresenter
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.PerFragment
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.util.Locale

@Module
class StoreModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerFragment
    internal fun providePresenter(
        interactor: StoreInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        appConverter: AppConverter,
        schedulers: SchedulersFactory
    ): StorePresenter = StorePresenterImpl(
        interactor,
        adapterPresenter,
        appConverter,
        schedulers,
        state
    )

    @Provides
    @PerFragment
    internal fun provideInteractor(
        api: StoreApi,
        locale: Locale,
        userDataInteractor: UserDataInteractor,
        schedulers: SchedulersFactory
    ): StoreInteractor = StoreInteractorImpl(api, locale, userDataInteractor, schedulers)

    @Provides
    @PerFragment
    internal fun provideResourceProvider(): AppsResourceProvider {
        return AppsResourceProviderImpl(context.resources)
    }

    @Provides
    @PerFragment
    internal fun provideAppsConverter(resourceProvider: AppsResourceProvider): AppConverter {
        return AppConverterImpl(resourceProvider)
    }

    @Provides
    @PerFragment
    internal fun provideAdapterPresenter(binder: ItemBinder): AdapterPresenter {
        return SimpleAdapterPresenter(binder, binder)
    }

    @Provides
    @PerFragment
    internal fun provideItemBinder(
        blueprintSet: Set<@JvmSuppressWildcards ItemBlueprint<*, *>>
    ): ItemBinder {
        return ItemBinder.Builder().apply {
            blueprintSet.forEach { registerItem(it) }
        }.build()
    }

    @Provides
    @IntoSet
    @PerFragment
    internal fun provideAppItemBlueprint(
        presenter: AppItemPresenter
    ): ItemBlueprint<*, *> = AppItemBlueprint(presenter)

    @Provides
    @PerFragment
    internal fun provideAppItemPresenter(presenter: StorePresenter) =
        AppItemPresenter(presenter)
}