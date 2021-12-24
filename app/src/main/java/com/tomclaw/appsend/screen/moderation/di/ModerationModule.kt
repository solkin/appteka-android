package com.tomclaw.appsend.screen.moderation.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.moderation.AppConverter
import com.tomclaw.appsend.screen.moderation.AppConverterImpl
import com.tomclaw.appsend.screen.moderation.AppsResourceProvider
import com.tomclaw.appsend.screen.moderation.AppsResourceProviderImpl
import com.tomclaw.appsend.screen.moderation.ModerationInteractor
import com.tomclaw.appsend.screen.moderation.ModerationInteractorImpl
import com.tomclaw.appsend.screen.moderation.ModerationPresenter
import com.tomclaw.appsend.screen.moderation.ModerationPresenterImpl
import com.tomclaw.appsend.screen.moderation.adapter.app.AppItemBlueprint
import com.tomclaw.appsend.screen.moderation.adapter.app.AppItemPresenter
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.util.Locale

@Module
class ModerationModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: ModerationInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        appConverter: AppConverter,
        schedulers: SchedulersFactory
    ): ModerationPresenter = ModerationPresenterImpl(
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
        userDataInteractor: UserDataInteractor,
        schedulers: SchedulersFactory
    ): ModerationInteractor = ModerationInteractorImpl(api, locale, userDataInteractor, schedulers)

    @Provides
    @PerActivity
    internal fun provideResourceProvider(): AppsResourceProvider {
        return AppsResourceProviderImpl(context.resources)
    }

    @Provides
    @PerActivity
    internal fun provideAppsConverter(resourceProvider: AppsResourceProvider): AppConverter {
        return AppConverterImpl(resourceProvider)
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
    internal fun provideAppItemPresenter(presenter: ModerationPresenter) =
        AppItemPresenter(presenter)

}
