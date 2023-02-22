package com.tomclaw.appsend.screen.upload.di

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.main.item.CommonItem
import com.tomclaw.appsend.screen.upload.UploadInteractor
import com.tomclaw.appsend.screen.upload.UploadInteractorImpl
import com.tomclaw.appsend.screen.upload.UploadPresenter
import com.tomclaw.appsend.screen.upload.UploadPresenterImpl
import com.tomclaw.appsend.screen.upload.adapter.select_app.SelectAppItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.select_app.SelectAppItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.selected_app.SelectedAppItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.selected_app.SelectedAppItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.selected_app.SelectedAppResourceProvider
import com.tomclaw.appsend.screen.upload.adapter.selected_app.SelectedAppResourceProviderImpl
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
    private val info: CommonItem?,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: UploadInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        schedulers: SchedulersFactory
    ): UploadPresenter = UploadPresenterImpl(info, interactor, adapterPresenter, schedulers, state)

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

}
