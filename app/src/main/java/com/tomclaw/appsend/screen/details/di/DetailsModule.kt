package com.tomclaw.appsend.screen.details.di

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.details.DetailsInteractor
import com.tomclaw.appsend.screen.details.DetailsInteractorImpl
import com.tomclaw.appsend.screen.details.DetailsPresenter
import com.tomclaw.appsend.screen.details.DetailsPresenterImpl
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItemPresenter
import com.tomclaw.appsend.screen.details.adapter.play.PlayItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.play.PlayItemPresenter
import com.tomclaw.appsend.screen.details.adapter.play.PlayResourceProvider
import com.tomclaw.appsend.screen.details.adapter.play.PlayResourceProviderImpl
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.util.Locale

@Module
class DetailsModule(
    private val appId: String?,
    private val packageName: String?,
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: DetailsInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        schedulers: SchedulersFactory
    ): DetailsPresenter = DetailsPresenterImpl(
        appId,
        packageName,
        interactor,
        adapterPresenter,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        userDataInteractor: UserDataInteractor,
        api: StoreApi,
        schedulers: SchedulersFactory
    ): DetailsInteractor = DetailsInteractorImpl(userDataInteractor, api, schedulers)

    @Provides
    @PerActivity
    internal fun provideAdapterPresenter(binder: ItemBinder): AdapterPresenter {
        return SimpleAdapterPresenter(binder, binder)
    }

    @Provides
    @PerActivity
    internal fun providePlayResourceProvider(): PlayResourceProvider {
        return PlayResourceProviderImpl(context.resources)
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
    internal fun provideHeaderItemBlueprint(
        presenter: HeaderItemPresenter
    ): ItemBlueprint<*, *> = HeaderItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideHeaderItemPresenter(
        presenter: DetailsPresenter
    ) = HeaderItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun providePlayItemBlueprint(
        presenter: PlayItemPresenter
    ): ItemBlueprint<*, *> = PlayItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun providePlayItemPresenter(
        locale: Locale,
        resourceProvider: PlayResourceProvider
    ) = PlayItemPresenter(locale, resourceProvider)

}