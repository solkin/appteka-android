package com.tomclaw.appsend.screen.users.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleAdapterPresenter
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.core.TimeProvider
import com.tomclaw.appsend.screen.users.PublishersInteractor
import com.tomclaw.appsend.screen.users.UsersInteractor
import com.tomclaw.appsend.screen.users.SubscribersInteractor
import com.tomclaw.appsend.screen.users.UsersPresenter
import com.tomclaw.appsend.screen.users.UsersPresenterImpl
import com.tomclaw.appsend.screen.users.UserConverter
import com.tomclaw.appsend.screen.users.UserConverterImpl
import com.tomclaw.appsend.screen.users.UsersType
import com.tomclaw.appsend.screen.users.adapter.subscriber.SubscriberItemBlueprint
import com.tomclaw.appsend.screen.users.adapter.subscriber.SubscriberItemPresenter
import com.tomclaw.appsend.screen.users.adapter.UsersResourceProvider
import com.tomclaw.appsend.screen.users.adapter.UsersResourceProviderImpl
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
    private val type: UsersType,
    private val userId: Int,
    private val state: Bundle?
) {

    @Provides
    @PerFragment
    internal fun providePresenter(
        interactor: UsersInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        converter: UserConverter,
        schedulers: SchedulersFactory
    ): UsersPresenter = UsersPresenterImpl(
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
    ): UsersInteractor = when (type) {
        UsersType.SUBSCRIBERS -> SubscribersInteractor(api, schedulers)
        UsersType.PUBLISHERS -> PublishersInteractor(api, schedulers)
    }

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
        return SimpleAdapterPresenter(binder)
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
        resourceProvider: UsersResourceProvider,
        presenter: UsersPresenter
    ) = SubscriberItemPresenter(locale, resourceProvider, presenter)

    @Provides
    @PerFragment
    internal fun provideSubscriberResourceProvider(timeProvider: TimeProvider): UsersResourceProvider {
        return UsersResourceProviderImpl(context, timeProvider)
    }

}
