package com.tomclaw.appsend.screen.post.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.post.PostConverter
import com.tomclaw.appsend.screen.post.PostConverterImpl
import com.tomclaw.appsend.screen.post.PostInteractor
import com.tomclaw.appsend.screen.post.PostInteractorImpl
import com.tomclaw.appsend.screen.post.PostPreferencesProvider
import com.tomclaw.appsend.screen.post.PostPreferencesProviderImpl
import com.tomclaw.appsend.screen.post.PostPresenter
import com.tomclaw.appsend.screen.post.PostPresenterImpl
import com.tomclaw.appsend.screen.post.adapter.screen_append.ScreenAppendItemBlueprint
import com.tomclaw.appsend.screen.post.adapter.screen_append.ScreenAppendItemPresenter
import com.tomclaw.appsend.screen.post.adapter.screen_image.ScreenImageItemBlueprint
import com.tomclaw.appsend.screen.post.adapter.screen_image.ScreenImageItemPresenter
import com.tomclaw.appsend.screen.post.adapter.screenshots.ScreenshotsItemBlueprint
import com.tomclaw.appsend.screen.post.adapter.screenshots.ScreenshotsItemPresenter
import com.tomclaw.appsend.screen.post.adapter.submit.SubmitItemBlueprint
import com.tomclaw.appsend.screen.post.adapter.submit.SubmitItemPresenter
import com.tomclaw.appsend.screen.post.adapter.text.TextItemBlueprint
import com.tomclaw.appsend.screen.post.adapter.text.TextItemPresenter
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.util.Locale
import javax.inject.Named

@Module
class PostModule(
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: PostInteractor,
        postConverter: PostConverter,
        @Named(POST_ADAPTER_PRESENTER) adapterPresenter: Lazy<AdapterPresenter>,
        preferences: PostPreferencesProvider,
        schedulers: SchedulersFactory
    ): PostPresenter = PostPresenterImpl(
        interactor,
        postConverter,
        adapterPresenter,
        preferences,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        locale: Locale,
        schedulers: SchedulersFactory
    ): PostInteractor = PostInteractorImpl(api, locale, schedulers)

    @Provides
    @PerActivity
    internal fun providePreferencesProvider(): PostPreferencesProvider {
        return PostPreferencesProviderImpl(context)
    }

    @Provides
    @PerActivity
    internal fun providePostConverterProvider(): PostConverter {
        return PostConverterImpl()
    }

    @Provides
    @PerActivity
    @Named(POST_ADAPTER_PRESENTER)
    internal fun providePostAdapterPresenter(binder: ItemBinder): AdapterPresenter {
        return SimpleAdapterPresenter(binder, binder)
    }

    @Provides
    @PerActivity
    @Named(SCREENSHOT_ADAPTER_PRESENTER)
    internal fun provideScreenshotAdapterPresenter(binder: ItemBinder): AdapterPresenter {
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
    internal fun provideScreenshotsItemBlueprint(
        presenter: ScreenshotsItemPresenter,
        @Named(SCREENSHOT_ADAPTER_PRESENTER) adapterPresenter: Lazy<AdapterPresenter>,
        binder: Lazy<ItemBinder>,
    ): ItemBlueprint<*, *> = ScreenshotsItemBlueprint(presenter, adapterPresenter, binder)

    @Provides
    @PerActivity
    internal fun provideScreenshotsItemPresenter(
        presenter: PostPresenter,
        @Named(SCREENSHOT_ADAPTER_PRESENTER) adapterPresenter: Lazy<AdapterPresenter>,
    ) = ScreenshotsItemPresenter(presenter, adapterPresenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideScreenAppendItemBlueprint(
        presenter: ScreenAppendItemPresenter
    ): ItemBlueprint<*, *> = ScreenAppendItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideScreenAppendItemPresenter(
        presenter: PostPresenter
    ) = ScreenAppendItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideScreenImageItemBlueprint(
        presenter: ScreenImageItemPresenter
    ): ItemBlueprint<*, *> = ScreenImageItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideScreenImageItemPresenter(
        presenter: PostPresenter
    ) = ScreenImageItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideTextItemBlueprint(
        presenter: TextItemPresenter
    ): ItemBlueprint<*, *> = TextItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideTextItemPresenter(
        presenter: PostPresenter
    ) = TextItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideSubmitItemBlueprint(
        presenter: SubmitItemPresenter
    ): ItemBlueprint<*, *> = SubmitItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideSubmitItemPresenter(
        presenter: PostPresenter
    ) = SubmitItemPresenter(presenter)

}

const val POST_ADAPTER_PRESENTER = "PostAdapterPresenter"
const val SCREENSHOT_ADAPTER_PRESENTER = "ScreenshotAdapterPresenter"
