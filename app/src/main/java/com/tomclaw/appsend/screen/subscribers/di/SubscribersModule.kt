package com.tomclaw.appsend.screen.subscribers.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.subscribers.SubscribersInteractor
import com.tomclaw.appsend.screen.subscribers.SubscribersInteractorImpl
import com.tomclaw.appsend.screen.subscribers.SubscribersPresenter
import com.tomclaw.appsend.screen.subscribers.SubscribersPresenterImpl
import com.tomclaw.appsend.screen.subscribers.UserConverter
import com.tomclaw.appsend.screen.subscribers.UserConverterImpl
import com.tomclaw.appsend.screen.subscribers.adapter.subscriber.SubscriberItemBlueprint
import com.tomclaw.appsend.screen.subscribers.adapter.subscriber.SubscriberItemPresenter
import com.tomclaw.appsend.screen.subscribers.adapter.subscriber.SubscriberResourceProvider
import com.tomclaw.appsend.screen.subscribers.adapter.subscriber.SubscriberResourceProviderImpl
import com.tomclaw.appsend.util.PerFragment
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.util.Locale

@Module
class SubscribersModule(
    private val context: Context,
    private val userId: Int,
    private val state: Bundle?
) {

    @Provides
    @PerFragment
    internal fun providePresenter(
        interactor: SubscribersInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        converter: UserConverter,
        schedulers: SchedulersFactory
    ): SubscribersPresenter = SubscribersPresenterImpl(
        userId,
        interactor,
        adapterPresenter,
        converter,
        schedulers,
        state
    )

    @Provides
    @PerFragment
    internal fun provideInteractor(
        api: StoreApi,
        schedulers: SchedulersFactory
    ): SubscribersInteractor = SubscribersInteractorImpl(
        api,
        schedulers
    )

    @Provides
    @PerFragment
    internal fun provideUserConverter(): UserConverter {
        return UserConverterImpl()
    }

    @Provides
    @PerFragment
    internal fun provideCategoryConverter(locale: Locale): CategoryConverter =
        CategoryConverterImpl(locale)

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
    internal fun provideUserItemBlueprint(
        presenter: SubscriberItemPresenter
    ): ItemBlueprint<*, *> = SubscriberItemBlueprint(presenter)

    @Provides
    @PerFragment
    internal fun provideAppItemPresenter(
        locale: Locale,
        resourceProvider: SubscriberResourceProvider,
        presenter: SubscribersPresenter
    ) = SubscriberItemPresenter(locale, resourceProvider, presenter)

    @Provides
    @PerFragment
    internal fun provideSubscriberResourceProvider(): SubscriberResourceProvider {
        return SubscriberResourceProviderImpl(context)
    }

}
