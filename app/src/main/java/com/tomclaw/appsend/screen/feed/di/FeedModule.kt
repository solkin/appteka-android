package com.tomclaw.appsend.screen.feed.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.feed.FeedConverter
import com.tomclaw.appsend.screen.feed.FeedConverterImpl
import com.tomclaw.appsend.screen.feed.FeedInteractor
import com.tomclaw.appsend.screen.feed.FeedInteractorImpl
import com.tomclaw.appsend.screen.feed.FeedPresenter
import com.tomclaw.appsend.screen.feed.FeedPresenterImpl
import com.tomclaw.appsend.screen.feed.adapter.post.PostItemBlueprint
import com.tomclaw.appsend.screen.feed.adapter.post.PostItemPresenter
import com.tomclaw.appsend.util.PerFragment
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.util.Locale

@Module
class FeedModule(
    private val context: Context,
    private val userId: Int,
    private val state: Bundle?
) {

    @Provides
    @PerFragment
    internal fun providePresenter(
        interactor: FeedInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        converter: FeedConverter,
        schedulers: SchedulersFactory
    ): FeedPresenter = FeedPresenterImpl(
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
    ): FeedInteractor = FeedInteractorImpl(api, schedulers)

    @Provides
    @PerFragment
    internal fun provideFeedConverter(): FeedConverter {
        return FeedConverterImpl()
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
    internal fun providePostItemBlueprint(
        presenter: PostItemPresenter
    ): ItemBlueprint<*, *> = PostItemBlueprint(presenter)

    @Provides
    @PerFragment
    internal fun provideFeedItemPresenter(
        locale: Locale,
        presenter: FeedPresenter,
    ) = PostItemPresenter(locale, presenter)

}
