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
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionItemPresenter
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionResourceProvider
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionResourceProviderImpl
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItemPresenter
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsItemPresenter
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsResourceProvider
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsResourceProviderImpl
import com.tomclaw.appsend.screen.details.adapter.play.PlayItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.play.PlayItemPresenter
import com.tomclaw.appsend.screen.details.adapter.play.PlayResourceProvider
import com.tomclaw.appsend.screen.details.adapter.play.PlayResourceProviderImpl
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItemPresenter
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
    internal fun provideDescriptionResourceProvider(locale: Locale): DescriptionResourceProvider {
        return DescriptionResourceProviderImpl(context.resources, locale)
    }

    @Provides
    @PerActivity
    internal fun providePermissionsResourceProvider(): PermissionsResourceProvider {
        return PermissionsResourceProviderImpl(context.resources)
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

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideDescriptionItemBlueprint(
        presenter: DescriptionItemPresenter
    ): ItemBlueprint<*, *> = DescriptionItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideDescriptionItemPresenter(
        resourceProvider: DescriptionResourceProvider
    ) = DescriptionItemPresenter(resourceProvider)

    @Provides
    @IntoSet
    @PerActivity
    internal fun providePermissionsItemBlueprint(
        presenter: PermissionsItemPresenter
    ): ItemBlueprint<*, *> = PermissionsItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun providePermissionsItemPresenter(
        resourceProvider: PermissionsResourceProvider,
        presenter: DetailsPresenter
    ) = PermissionsItemPresenter(resourceProvider, presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideScoresItemBlueprint(
        presenter: ScoresItemPresenter
    ): ItemBlueprint<*, *> = ScoresItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideScoresItemPresenter(
        presenter: DetailsPresenter
    ) = ScoresItemPresenter(presenter)

}