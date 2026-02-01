package com.tomclaw.appsend.screen.ratings.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleAdapterPresenter
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.di.DATE_FORMATTER
import com.tomclaw.appsend.screen.ratings.RatingConverter
import com.tomclaw.appsend.screen.ratings.RatingConverterImpl
import com.tomclaw.appsend.screen.ratings.RatingsInteractor
import com.tomclaw.appsend.screen.ratings.RatingsInteractorImpl
import com.tomclaw.appsend.screen.ratings.RatingsPreferencesProvider
import com.tomclaw.appsend.screen.ratings.RatingsPreferencesProviderImpl
import com.tomclaw.appsend.screen.ratings.RatingsPresenter
import com.tomclaw.appsend.screen.ratings.RatingsPresenterImpl
import com.tomclaw.appsend.screen.ratings.adapter.rating.RatingItemBlueprint
import com.tomclaw.appsend.screen.ratings.adapter.rating.RatingItemPresenter
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.text.DateFormat
import java.util.Locale
import javax.inject.Named

@Module
class RatingsModule(
    private val context: Context,
    private val appId: String,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: RatingsInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        converter: RatingConverter,
        schedulers: SchedulersFactory
    ): RatingsPresenter = RatingsPresenterImpl(
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
        schedulers: SchedulersFactory
    ): RatingsInteractor = RatingsInteractorImpl(
        api,
        appId,
        schedulers
    )

    @Provides
    @PerActivity
    internal fun provideRatingConverter(locale: Locale): RatingConverter {
        return RatingConverterImpl(locale)
    }

    @Provides
    @PerActivity
    internal fun provideCategoryConverter(locale: Locale): CategoryConverter =
        CategoryConverterImpl(locale)

    @Provides
    @PerActivity
    internal fun provideRatingsPreferencesProvider(): RatingsPreferencesProvider =
        RatingsPreferencesProviderImpl(context)

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
    internal fun provideRatingItemBlueprint(
        presenter: RatingItemPresenter,
        preferences: RatingsPreferencesProvider,
    ): ItemBlueprint<*, *> = RatingItemBlueprint(presenter, preferences)

    @Provides
    @PerActivity
    internal fun provideRatingItemPresenter(
        @Named(DATE_FORMATTER) dateFormatter: DateFormat,
        presenter: RatingsPresenter,
    ) = RatingItemPresenter(dateFormatter, presenter)

}
