package com.tomclaw.appsend.screen.discuss.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.screen.discuss.DiscussInteractor
import com.tomclaw.appsend.screen.discuss.DiscussInteractorImpl
import com.tomclaw.appsend.screen.discuss.DiscussPreferencesProvider
import com.tomclaw.appsend.screen.discuss.DiscussPreferencesProviderImpl
import com.tomclaw.appsend.screen.discuss.DiscussPresenter
import com.tomclaw.appsend.screen.discuss.DiscussPresenterImpl
import com.tomclaw.appsend.screen.discuss.TopicConverter
import com.tomclaw.appsend.screen.discuss.TopicConverterImpl
import com.tomclaw.appsend.screen.discuss.adapter.topic.TopicItemBlueprint
import com.tomclaw.appsend.screen.discuss.adapter.topic.TopicItemPresenter
import com.tomclaw.appsend.util.PerFragment
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
class DiscussModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerFragment
    internal fun providePresenter(
        converter: TopicConverter,
        preferences: DiscussPreferencesProvider,
        interactor: DiscussInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        schedulers: SchedulersFactory
    ): DiscussPresenter = DiscussPresenterImpl(
        converter,
        preferences,
        interactor,
        adapterPresenter,
        schedulers,
        state
    )

    @Provides
    @PerFragment
    internal fun provideInteractor(
        schedulers: SchedulersFactory
    ): DiscussInteractor = DiscussInteractorImpl(schedulers)

    @Provides
    @PerFragment
    internal fun provideDiscussPreferencesProvider(): DiscussPreferencesProvider =
        DiscussPreferencesProviderImpl(context)

    @Provides
    @PerFragment
    internal fun provideTopicConverter(): TopicConverter = TopicConverterImpl()

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
    internal fun provideTopicItemBlueprint(
        presenter: TopicItemPresenter
    ): ItemBlueprint<*, *> = TopicItemBlueprint(presenter)

    @Provides
    @PerFragment
    internal fun provideTopicItemPresenter(presenter: DiscussPresenter) =
        TopicItemPresenter(presenter)

}