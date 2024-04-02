package com.tomclaw.appsend.screen.profile.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.di.DATE_FORMATTER
import com.tomclaw.appsend.screen.profile.ProfileConverter
import com.tomclaw.appsend.screen.profile.ProfileConverterImpl
import com.tomclaw.appsend.screen.profile.ProfileInteractor
import com.tomclaw.appsend.screen.profile.ProfileInteractorImpl
import com.tomclaw.appsend.screen.profile.ProfilePresenter
import com.tomclaw.appsend.screen.profile.ProfilePresenterImpl
import com.tomclaw.appsend.screen.profile.adapter.app.AppItemBlueprint
import com.tomclaw.appsend.screen.profile.adapter.app.AppItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.favorites.FavoritesItemBlueprint
import com.tomclaw.appsend.screen.profile.adapter.favorites.FavoritesItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderItemBlueprint
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderResourceProvider
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderResourceProviderImpl
import com.tomclaw.appsend.screen.profile.adapter.placeholder.PlaceholderItemBlueprint
import com.tomclaw.appsend.screen.profile.adapter.placeholder.PlaceholderItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.review.ReviewItemBlueprint
import com.tomclaw.appsend.screen.profile.adapter.review.ReviewItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.reviews.ReviewsItemBlueprint
import com.tomclaw.appsend.screen.profile.adapter.reviews.ReviewsItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.uploads.UploadsItemBlueprint
import com.tomclaw.appsend.screen.profile.adapter.uploads.UploadsItemPresenter
import com.tomclaw.appsend.util.PerFragment
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.text.DateFormat
import java.util.Locale
import javax.inject.Named

@Module
class ProfileModule(
    private val userId: Int,
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerFragment
    internal fun providePresenter(
        interactor: ProfileInteractor,
        converter: ProfileConverter,
        @Named(PROFILE_ADAPTER_PRESENTER) adapterPresenter: Lazy<AdapterPresenter>,
        schedulers: SchedulersFactory
    ): ProfilePresenter = ProfilePresenterImpl(
        userId,
        interactor,
        converter,
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
    internal fun provideConverter(): ProfileConverter = ProfileConverterImpl()

    @Provides
    @Named(PROFILE_ADAPTER_PRESENTER)
    @PerFragment
    internal fun provideProfileAdapterPresenter(binder: ItemBinder): AdapterPresenter {
        return SimpleAdapterPresenter(binder, binder)
    }

    @Provides
    @Named(UPLOADS_ADAPTER_PRESENTER)
    @PerFragment
    internal fun provideUploadsAdapterPresenter(binder: ItemBinder): AdapterPresenter {
        return SimpleAdapterPresenter(binder, binder)
    }

    @Provides
    @Named(RATINGS_ADAPTER_PRESENTER)
    @PerFragment
    internal fun provideRatingsAdapterPresenter(binder: ItemBinder): AdapterPresenter {
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
    internal fun provideHeaderResourceProvider(
        context: Context,
    ): HeaderResourceProvider = HeaderResourceProviderImpl(context)

    @Provides
    @PerFragment
    internal fun provideHeaderItemPresenter(
        presenter: ProfilePresenter,
        resourceProvider: HeaderResourceProvider,
        locale: Locale,
    ) = HeaderItemPresenter(presenter, resourceProvider, locale)

    @Provides
    @IntoSet
    @PerFragment
    internal fun provideAppItemBlueprint(
        presenter: AppItemPresenter
    ): ItemBlueprint<*, *> = AppItemBlueprint(presenter)

    @Provides
    @PerFragment
    internal fun provideAppItemPresenter(
        presenter: UploadsItemPresenter,
    ) = AppItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerFragment
    internal fun provideUploadsItemBlueprint(
        presenter: UploadsItemPresenter,
        @Named(UPLOADS_ADAPTER_PRESENTER) adapterPresenter: Lazy<AdapterPresenter>,
        binder: Lazy<ItemBinder>,
    ): ItemBlueprint<*, *> = UploadsItemBlueprint(
        presenter,
        adapterPresenter,
        binder
    )

    @Provides
    @PerFragment
    internal fun provideUploadsItemPresenter(
        presenter: ProfilePresenter,
        @Named(UPLOADS_ADAPTER_PRESENTER) adapterPresenter: Lazy<AdapterPresenter>,
    ) = UploadsItemPresenter(
        presenter,
        adapterPresenter
    )

    @Provides
    @IntoSet
    @PerFragment
    internal fun provideRatingItemBlueprint(
        presenter: ReviewItemPresenter
    ): ItemBlueprint<*, *> = ReviewItemBlueprint(presenter)

    @Provides
    @PerFragment
    internal fun provideRatingItemPresenter(
        @Named(DATE_FORMATTER) dateFormatter: DateFormat,
        presenter: ReviewsItemPresenter,
    ) = ReviewItemPresenter(dateFormatter, presenter)

    @Provides
    @IntoSet
    @PerFragment
    internal fun provideRatingsItemBlueprint(
        presenter: ReviewsItemPresenter,
        @Named(RATINGS_ADAPTER_PRESENTER) adapterPresenter: Lazy<AdapterPresenter>,
        binder: Lazy<ItemBinder>,
    ): ItemBlueprint<*, *> = ReviewsItemBlueprint(
        presenter,
        adapterPresenter,
        binder
    )

    @Provides
    @PerFragment
    internal fun provideRatingsItemPresenter(
        presenter: ProfilePresenter,
        @Named(RATINGS_ADAPTER_PRESENTER) adapterPresenter: Lazy<AdapterPresenter>,
    ) = ReviewsItemPresenter(
        presenter,
        adapterPresenter
    )

    @Provides
    @IntoSet
    @PerFragment
    internal fun provideFavoritesItemBlueprint(
        presenter: FavoritesItemPresenter
    ): ItemBlueprint<*, *> = FavoritesItemBlueprint(presenter)

    @Provides
    @PerFragment
    internal fun provideFavoritesItemPresenter(
        presenter: ProfilePresenter,
    ) = FavoritesItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerFragment
    internal fun providePlaceholderItemBlueprint(
        presenter: PlaceholderItemPresenter
    ): ItemBlueprint<*, *> = PlaceholderItemBlueprint(presenter)

    @Provides
    @PerFragment
    internal fun providePlaceholderItemPresenter() = PlaceholderItemPresenter()

}

const val PROFILE_ADAPTER_PRESENTER = "ProfileAdapterPresenter"
const val UPLOADS_ADAPTER_PRESENTER = "UploadsAdapterPresenter"
const val RATINGS_ADAPTER_PRESENTER = "RatingsAdapterPresenter"
