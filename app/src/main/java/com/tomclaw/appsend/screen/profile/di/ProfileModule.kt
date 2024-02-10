package com.tomclaw.appsend.screen.profile.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.profile.ProfileInteractor
import com.tomclaw.appsend.screen.profile.ProfileInteractorImpl
import com.tomclaw.appsend.screen.profile.ProfilePresenter
import com.tomclaw.appsend.screen.profile.ProfilePresenterImpl
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderItemBlueprint
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderItemPresenter
import com.tomclaw.appsend.screen.topics.TopicsPresenter
import com.tomclaw.appsend.screen.topics.adapter.topic.TopicItemBlueprint
import com.tomclaw.appsend.screen.topics.adapter.topic.TopicItemPresenter
import com.tomclaw.appsend.util.PerFragment
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
class ProfileModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerFragment
    internal fun providePresenter(
        interactor: ProfileInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        schedulers: SchedulersFactory
    ): ProfilePresenter = ProfilePresenterImpl(
        interactor,
        adapterPresenter,
        schedulers,
        state
    )

    @Provides
    @PerFragment
    internal fun provideInteractor(
        api: StoreApi,
        schedulers: SchedulersFactory
    ): ProfileInteractor = ProfileInteractorImpl(api, schedulers)

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
    internal fun provideHeaderItemBlueprint(
        presenter: HeaderItemPresenter
    ): ItemBlueprint<*, *> = HeaderItemBlueprint(presenter)

    @Provides
    @PerFragment
    internal fun provideHeaderItemPresenter(presenter: ProfilePresenter) =
        HeaderItemPresenter(presenter)

}