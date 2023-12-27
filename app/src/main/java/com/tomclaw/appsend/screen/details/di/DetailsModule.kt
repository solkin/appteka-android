package com.tomclaw.appsend.screen.details.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.di.DATE_FORMATTER
import com.tomclaw.appsend.download.DownloadManager
import com.tomclaw.appsend.screen.details.DetailsConverter
import com.tomclaw.appsend.screen.details.DetailsConverterImpl
import com.tomclaw.appsend.screen.details.DetailsInteractor
import com.tomclaw.appsend.screen.details.DetailsInteractorImpl
import com.tomclaw.appsend.screen.details.DetailsPreferencesProvider
import com.tomclaw.appsend.screen.details.DetailsPreferencesProviderImpl
import com.tomclaw.appsend.screen.details.DetailsPresenter
import com.tomclaw.appsend.screen.details.DetailsPresenterImpl
import com.tomclaw.appsend.screen.details.DetailsResourceProvider
import com.tomclaw.appsend.screen.details.DetailsResourceProviderImpl
import com.tomclaw.appsend.screen.details.adapter.controls.ControlsItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.controls.ControlsItemPresenter
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionItemPresenter
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionResourceProvider
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionResourceProviderImpl
import com.tomclaw.appsend.screen.details.adapter.discuss.DiscussItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.discuss.DiscussItemPresenter
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItemPresenter
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsItemPresenter
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsResourceProvider
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsResourceProviderImpl
import com.tomclaw.appsend.screen.details.adapter.play.PlayItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.play.PlayItemPresenter
import com.tomclaw.appsend.screen.details.adapter.play.PlayResourceProvider
import com.tomclaw.appsend.screen.details.adapter.play.PlayResourceProviderImpl
import com.tomclaw.appsend.screen.details.adapter.rating.RatingItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.rating.RatingItemPresenter
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItemPresenter
import com.tomclaw.appsend.screen.details.adapter.screenshots.ScreenshotsAdapter
import com.tomclaw.appsend.screen.details.adapter.screenshots.ScreenshotsItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.screenshots.ScreenshotsItemPresenter
import com.tomclaw.appsend.screen.details.adapter.status.StatusItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.status.StatusItemPresenter
import com.tomclaw.appsend.screen.details.adapter.user_rate.UserRateItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.user_rate.UserRateItemPresenter
import com.tomclaw.appsend.screen.details.adapter.user_review.UserReviewItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.user_review.UserReviewItemPresenter
import com.tomclaw.appsend.screen.details.adapter.whats_new.WhatsNewItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.whats_new.WhatsNewItemPresenter
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
class DetailsModule(
    private val appId: String?,
    private val packageName: String?,
    private val moderation: Boolean,
    private val finishOnly: Boolean,
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: DetailsInteractor,
        resourceProvider: DetailsResourceProvider,
        adapterPresenter: Lazy<AdapterPresenter>,
        detailsConverter: DetailsConverter,
        packageObserver: PackageObserver,
        downloadManager: DownloadManager,
        schedulers: SchedulersFactory
    ): DetailsPresenter = DetailsPresenterImpl(
        appId,
        packageName,
        moderation,
        finishOnly,
        interactor,
        resourceProvider,
        adapterPresenter,
        detailsConverter,
        packageObserver,
        downloadManager,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        schedulers: SchedulersFactory
    ): DetailsInteractor = DetailsInteractorImpl(api, schedulers)

    @Provides
    @PerActivity
    internal fun provideDetailsResourceProvider(
        locale: Locale
    ): DetailsResourceProvider = DetailsResourceProviderImpl(resources = context.resources, locale)

    @Provides
    @PerActivity
    internal fun provideDetailsConverterProvider(
        resourceProvider: DetailsResourceProvider
    ): DetailsConverter = DetailsConverterImpl(resourceProvider)

    @Provides
    @PerActivity
    internal fun provideAdapterPresenter(binder: ItemBinder): AdapterPresenter {
        return SimpleAdapterPresenter(binder, binder)
    }

    @Provides
    @PerActivity
    internal fun provideDetailsPreferencesProvider(): DetailsPreferencesProvider {
        return DetailsPreferencesProviderImpl(context)
    }

    @Provides
    @PerActivity
    internal fun providePlayResourceProvider(): PlayResourceProvider {
        return PlayResourceProviderImpl(context.resources)
    }

    @Provides
    @PerActivity
    internal fun provideDescriptionResourceProvider(locale: Locale): DescriptionResourceProvider {
        return DescriptionResourceProviderImpl(context.resources, locale)
    }

    @Provides
    @PerActivity
    internal fun providePermissionsResourceProvider(): PermissionsResourceProvider {
        return PermissionsResourceProviderImpl(context.resources)
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
    internal fun provideHeaderItemBlueprint(
        presenter: HeaderItemPresenter
    ): ItemBlueprint<*, *> = HeaderItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideHeaderItemPresenter(
        locale: Locale,
        presenter: DetailsPresenter
    ) = HeaderItemPresenter(locale, presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun providePlayItemBlueprint(
        presenter: PlayItemPresenter
    ): ItemBlueprint<*, *> = PlayItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun providePlayItemPresenter(
        locale: Locale,
        resourceProvider: PlayResourceProvider
    ) = PlayItemPresenter(locale, resourceProvider)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideWhatsNewItemBlueprint(
        presenter: WhatsNewItemPresenter
    ): ItemBlueprint<*, *> = WhatsNewItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideWhatsNewItemPresenter(
        presenter: DetailsPresenter,
    ) = WhatsNewItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideDescriptionItemBlueprint(
        presenter: DescriptionItemPresenter
    ): ItemBlueprint<*, *> = DescriptionItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideDescriptionItemPresenter(
        presenter: DetailsPresenter,
        resourceProvider: DescriptionResourceProvider
    ) = DescriptionItemPresenter(presenter, resourceProvider)

    @Provides
    @IntoSet
    @PerActivity
    internal fun providePermissionsItemBlueprint(
        presenter: PermissionsItemPresenter
    ): ItemBlueprint<*, *> = PermissionsItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun providePermissionsItemPresenter(
        resourceProvider: PermissionsResourceProvider,
        presenter: DetailsPresenter
    ) = PermissionsItemPresenter(resourceProvider, presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideScoresItemBlueprint(
        presenter: ScoresItemPresenter
    ): ItemBlueprint<*, *> = ScoresItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideScoresItemPresenter(
        presenter: DetailsPresenter
    ) = ScoresItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideRatingItemBlueprint(
        presenter: RatingItemPresenter
    ): ItemBlueprint<*, *> = RatingItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideRatingItemPresenter(
        @Named(DATE_FORMATTER) dateFormatter: DateFormat,
        presenter: DetailsPresenter
    ) = RatingItemPresenter(dateFormatter, presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideControlsItemBlueprint(
        presenter: ControlsItemPresenter
    ): ItemBlueprint<*, *> = ControlsItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideControlsItemPresenter(
        presenter: DetailsPresenter,
    ) = ControlsItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideUserRateItemBlueprint(
        presenter: UserRateItemPresenter
    ): ItemBlueprint<*, *> = UserRateItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideUserRateItemPresenter(
        presenter: DetailsPresenter,
    ) = UserRateItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideUserReviewItemBlueprint(
        presenter: UserReviewItemPresenter
    ): ItemBlueprint<*, *> = UserReviewItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideUserReviewItemPresenter(
        @Named(DATE_FORMATTER) dateFormatter: DateFormat,
        locale: Locale,
        presenter: DetailsPresenter,
    ) = UserReviewItemPresenter(dateFormatter, locale, presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideDiscussItemBlueprint(
        presenter: DiscussItemPresenter
    ): ItemBlueprint<*, *> = DiscussItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideDiscussItemPresenter(
        presenter: DetailsPresenter,
    ) = DiscussItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideStatusItemBlueprint(
        presenter: StatusItemPresenter
    ): ItemBlueprint<*, *> = StatusItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideStatusItemPresenter(
        presenter: DetailsPresenter,
    ) = StatusItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideScreenshotsItemBlueprint(
        presenter: ScreenshotsItemPresenter,
        adapter: ScreenshotsAdapter,
    ): ItemBlueprint<*, *> = ScreenshotsItemBlueprint(presenter, adapter)

    @Provides
    @PerActivity
    internal fun provideScreenshotsItemPresenter(
        presenter: DetailsPresenter,
    ) = ScreenshotsItemPresenter(presenter)

    @Provides
    @PerActivity
    internal fun provideScreenshotsItemAdapter(
        presenter: DetailsPresenter,
    ) = ScreenshotsAdapter()

}
