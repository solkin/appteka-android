package com.tomclaw.appsend.screen.reviews.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.di.DATE_FORMATTER
import com.tomclaw.appsend.screen.favorite.AppConverter
import com.tomclaw.appsend.screen.favorite.AppConverterImpl
import com.tomclaw.appsend.screen.favorite.AppsResourceProvider
import com.tomclaw.appsend.screen.favorite.AppsResourceProviderImpl
import com.tomclaw.appsend.screen.favorite.FavoriteInteractor
import com.tomclaw.appsend.screen.favorite.FavoriteInteractorImpl
import com.tomclaw.appsend.screen.favorite.FavoritePresenter
import com.tomclaw.appsend.screen.favorite.FavoritePresenterImpl
import com.tomclaw.appsend.screen.favorite.adapter.app.AppItemBlueprint
import com.tomclaw.appsend.screen.favorite.adapter.app.AppItemPresenter
import com.tomclaw.appsend.screen.reviews.ReviewConverter
import com.tomclaw.appsend.screen.reviews.ReviewConverterImpl
import com.tomclaw.appsend.screen.reviews.ReviewsInteractor
import com.tomclaw.appsend.screen.reviews.ReviewsInteractorImpl
import com.tomclaw.appsend.screen.reviews.ReviewsPresenter
import com.tomclaw.appsend.screen.reviews.ReviewsPresenterImpl
import com.tomclaw.appsend.screen.reviews.adapter.review.ReviewItemBlueprint
import com.tomclaw.appsend.screen.reviews.adapter.review.ReviewItemPresenter
import com.tomclaw.appsend.util.PackageObserver
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
class ReviewsModule(
    private val context: Context,
    private val userId: Int,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: ReviewsInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        converter: ReviewConverter,
        schedulers: SchedulersFactory
    ): ReviewsPresenter = ReviewsPresenterImpl(
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
    ): ReviewsInteractor = ReviewsInteractorImpl(
        api,
        userId,
        locale,
        schedulers
    )

    @Provides
    @PerActivity
    internal fun provideReviewConverter(): ReviewConverter {
        return ReviewConverterImpl()
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
    internal fun provideReviewItemBlueprint(
        presenter: ReviewItemPresenter
    ): ItemBlueprint<*, *> = ReviewItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideReviewItemPresenter(
        @Named(DATE_FORMATTER) dateFormatter: DateFormat,
        presenter: ReviewsPresenter,
    ) = ReviewItemPresenter(dateFormatter, presenter)

}
