package com.tomclaw.appsend.screen.topics.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.topics.TopicConverter
import com.tomclaw.appsend.screen.topics.TopicConverterImpl
import com.tomclaw.appsend.screen.topics.TopicsInteractor
import com.tomclaw.appsend.screen.topics.TopicsInteractorImpl
import com.tomclaw.appsend.screen.topics.TopicsPreferencesProvider
import com.tomclaw.appsend.screen.topics.TopicsPreferencesProviderImpl
import com.tomclaw.appsend.screen.topics.TopicsPresenter
import com.tomclaw.appsend.screen.topics.TopicsPresenterImpl
import com.tomclaw.appsend.screen.topics.TopicsResourceProvider
import com.tomclaw.appsend.screen.topics.TopicsResourceProviderImpl
import com.tomclaw.appsend.screen.topics.adapter.topic.TopicItemBlueprint
import com.tomclaw.appsend.screen.topics.adapter.topic.TopicItemPresenter
import com.tomclaw.appsend.util.PerFragment
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
class TopicsModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerFragment
    internal fun providePresenter(
        converter: TopicConverter,
        preferences: TopicsPreferencesProvider,
        topicsInteractor: TopicsInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        schedulers: SchedulersFactory
    ): TopicsPresenter = TopicsPresenterImpl(
        converter,
        preferences,
        topicsInteractor,
        adapterPresenter,
        schedulers,
        state
    )

    @Provides
    @PerFragment
    internal fun provideInteractor(
        api: StoreApi,
        schedulers: SchedulersFactory
    ): TopicsInteractor = TopicsInteractorImpl(api, schedulers)

    @Provides
    @PerFragment
    internal fun providePreferencesProvider(): TopicsPreferencesProvider =
        TopicsPreferencesProviderImpl(context)

    @Provides
    @PerFragment
    internal fun provideTopicConverter(
        resourceProvider: TopicsResourceProvider
    ): TopicConverter = TopicConverterImpl(resourceProvider)

    @Provides
    @PerFragment
    internal fun provideResourceProvider(): TopicsResourceProvider =
        TopicsResourceProviderImpl(context.resources)

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
    internal fun provideTopicItemPresenter(presenter: TopicsPresenter) =
        TopicItemPresenter(presenter)

}
