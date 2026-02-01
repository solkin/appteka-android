package com.tomclaw.appsend.screen.moderation.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleAdapterPresenter
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.details.adapter.abi.AbiResourceProvider
import com.tomclaw.appsend.screen.details.adapter.abi.AbiResourceProviderImpl
import com.tomclaw.appsend.screen.moderation.AppConverter
import com.tomclaw.appsend.screen.moderation.AppConverterImpl
import com.tomclaw.appsend.screen.moderation.AppsResourceProvider
import com.tomclaw.appsend.screen.moderation.AppsResourceProviderImpl
import com.tomclaw.appsend.screen.moderation.ModerationInteractor
import com.tomclaw.appsend.screen.moderation.ModerationInteractorImpl
import com.tomclaw.appsend.screen.moderation.ModerationPresenter
import com.tomclaw.appsend.screen.moderation.ModerationPresenterImpl
import com.tomclaw.appsend.user.ModerationProvider
import com.tomclaw.appsend.screen.moderation.adapter.app.AppItemBlueprint
import com.tomclaw.appsend.screen.moderation.adapter.app.AppItemPresenter
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
        moderationProvider: ModerationProvider,
        adapterPresenter: Lazy<AdapterPresenter>,
        appConverter: AppConverter,
        schedulers: SchedulersFactory
    ): ModerationPresenter = ModerationPresenterImpl(
        interactor,
        moderationProvider,
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
    ): ModerationInteractor = ModerationInteractorImpl(api, locale, schedulers)

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
        abiResourceProvider: AbiResourceProvider
    ): AppConverter {
        return AppConverterImpl(resourceProvider, categoryConverter, abiResourceProvider)
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
    internal fun provideAppItemPresenter(presenter: ModerationPresenter) =
        AppItemPresenter(presenter)

}
