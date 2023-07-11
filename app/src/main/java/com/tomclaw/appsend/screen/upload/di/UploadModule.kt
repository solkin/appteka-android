package com.tomclaw.appsend.screen.upload.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.categories.CategoriesInteractor
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.upload.UploadConverter
import com.tomclaw.appsend.screen.upload.UploadConverterImpl
import com.tomclaw.appsend.screen.upload.UploadInteractor
import com.tomclaw.appsend.screen.upload.UploadInteractorImpl
import com.tomclaw.appsend.screen.upload.UploadPreferencesProvider
import com.tomclaw.appsend.screen.upload.UploadPreferencesProviderImpl
import com.tomclaw.appsend.screen.upload.UploadPresenter
import com.tomclaw.appsend.screen.upload.UploadPresenterImpl
import com.tomclaw.appsend.screen.upload.UploadResourceProvider
import com.tomclaw.appsend.screen.upload.UploadResourceProviderImpl
import com.tomclaw.appsend.screen.upload.adapter.category.SelectCategoryItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.category.SelectCategoryItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.description.DescriptionItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.description.DescriptionItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.exclusive.ExclusiveItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.exclusive.ExclusiveItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.notice.NoticeItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.notice.NoticeItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.open_source.OpenSourceItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.open_source.OpenSourceItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.other_versions.OtherVersionsItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.other_versions.OtherVersionsItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.select_app.SelectAppItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.select_app.SelectAppItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.selected_app.SelectedAppItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.selected_app.SelectedAppItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.selected_app.SelectedAppResourceProvider
import com.tomclaw.appsend.screen.upload.adapter.selected_app.SelectedAppResourceProviderImpl
import com.tomclaw.appsend.screen.upload.adapter.submit.SubmitItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.submit.SubmitItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.whats_new.WhatsNewItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.whats_new.WhatsNewItemPresenter
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadInfo
import com.tomclaw.appsend.upload.UploadManager
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.util.Locale

@Module
class UploadModule(
    private val context: Context,
    private val pkg: UploadPackage?,
    private val apk: UploadApk?,
    private val meta: UploadInfo?,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: UploadInteractor,
        categoriesInteractor: CategoriesInteractor,
        categoryConverter: CategoryConverter,
        uploadConverter: UploadConverter,
        adapterPresenter: Lazy<AdapterPresenter>,
        uploadManager: UploadManager,
        schedulers: SchedulersFactory
    ): UploadPresenter = UploadPresenterImpl(
        pkg,
        apk,
        meta,
        interactor,
        categoriesInteractor,
        categoryConverter,
        uploadConverter,
        adapterPresenter,
        uploadManager,
        schedulers,
        state
    )

    @Provides
    @PerActivity
    internal fun provideInteractor(
        api: StoreApi,
        locale: Locale,
        userDataInteractor: UserDataInteractor,
        schedulers: SchedulersFactory
    ): UploadInteractor = UploadInteractorImpl(api, locale, userDataInteractor, schedulers)

    @Provides
    @PerActivity
    internal fun provideCategoryConverter(locale: Locale): CategoryConverter =
        CategoryConverterImpl(locale)

    @Provides
    @PerActivity
    internal fun provideUploadPreferencesProvider(): UploadPreferencesProvider {
        return UploadPreferencesProviderImpl(context)
    }

    @Provides
    @PerActivity
    internal fun provideUploadResourceProvider(
        locale: Locale
    ): UploadResourceProvider {
        return UploadResourceProviderImpl(context.resources, locale)
    }

    @Provides
    @PerActivity
    internal fun provideUploadConverterProvider(
        resourceProvider: UploadResourceProvider
    ): UploadConverter {
        return UploadConverterImpl(resourceProvider)
    }

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
    internal fun provideSelectAppItemBlueprint(
        presenter: SelectAppItemPresenter
    ): ItemBlueprint<*, *> = SelectAppItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideSelectAppItemPresenter(
        presenter: UploadPresenter
    ) = SelectAppItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideSelectedAppItemBlueprint(
        presenter: SelectedAppItemPresenter
    ): ItemBlueprint<*, *> = SelectedAppItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideSelectedAppItemPresenter(
        presenter: UploadPresenter,
        resourceProvider: SelectedAppResourceProvider
    ) = SelectedAppItemPresenter(presenter, resourceProvider)

    @Provides
    @PerActivity
    internal fun provideSelectedAppResourceProvider(): SelectedAppResourceProvider =
        SelectedAppResourceProviderImpl(context.resources)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideNoticeItemBlueprint(
        presenter: NoticeItemPresenter
    ): ItemBlueprint<*, *> = NoticeItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideNoticeItemPresenter(
        presenter: UploadPresenter
    ) = NoticeItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideSelectCategoryItemBlueprint(
        presenter: SelectCategoryItemPresenter
    ): ItemBlueprint<*, *> = SelectCategoryItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideSelectCategoryItemPresenter(
        presenter: UploadPresenter
    ) = SelectCategoryItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideWhatsNewItemBlueprint(
        presenter: WhatsNewItemPresenter
    ): ItemBlueprint<*, *> = WhatsNewItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideWhatsNewItemPresenter(
        presenter: UploadPresenter
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
        presenter: UploadPresenter
    ) = DescriptionItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideExclusiveItemBlueprint(
        presenter: ExclusiveItemPresenter
    ): ItemBlueprint<*, *> = ExclusiveItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideExclusiveItemPresenter(
        presenter: UploadPresenter
    ) = ExclusiveItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideOpenSourceItemBlueprint(
        presenter: OpenSourceItemPresenter
    ): ItemBlueprint<*, *> = OpenSourceItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideOpenSourceItemPresenter(
        presenter: UploadPresenter
    ) = OpenSourceItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideSubmitItemBlueprint(
        presenter: SubmitItemPresenter
    ): ItemBlueprint<*, *> = SubmitItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideSubmitItemPresenter(
        presenter: UploadPresenter
    ) = SubmitItemPresenter(presenter)

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideOtherVersionsItemBlueprint(
        presenter: OtherVersionsItemPresenter
    ): ItemBlueprint<*, *> = OtherVersionsItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideOtherVersionsItemPresenter(
        presenter: UploadPresenter
    ) = OtherVersionsItemPresenter(presenter)

}
