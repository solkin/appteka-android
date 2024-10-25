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
import com.tomclaw.appsend.screen.subscribers.adapter.user.UserItemBlueprint
import com.tomclaw.appsend.screen.subscribers.adapter.user.UserItemPresenter
import com.tomclaw.appsend.util.PackageObserver
import com.tomclaw.appsend.util.PerActivity
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
    @PerActivity
    internal fun providePresenter(
        interactor: SubscribersInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        converter: UserConverter,
        schedulers: SchedulersFactory
    ): SubscribersPresenter = SubscribersPresenterImpl(
        interactor,
        adapterPresenter,
        converter,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        locale: Locale,
        schedulers: SchedulersFactory
    ): SubscribersInteractor = SubscribersInteractorImpl(
        api,
        userId,
        locale,
        schedulers
    )

    @Provides
    @PerActivity
    internal fun provideUserConverter(
        categoryConverter: CategoryConverter,
        packageObserver: PackageObserver
    ): UserConverter {
        return UserConverterImpl(categoryConverter, packageObserver)
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
    internal fun provideUserItemBlueprint(
        presenter: UserItemPresenter
    ): ItemBlueprint<*, *> = UserItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideAppItemPresenter(
        presenter: SubscribersPresenter
    ) = UserItemPresenter(presenter)

}
