package com.tomclaw.appsend.screen.upload.di

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.screen.details.DetailsPresenter
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItemBlueprint
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItemPresenter
import com.tomclaw.appsend.screen.upload.UploadInteractor
import com.tomclaw.appsend.screen.upload.UploadInteractorImpl
import com.tomclaw.appsend.screen.upload.UploadPresenter
import com.tomclaw.appsend.screen.upload.UploadPresenterImpl
import com.tomclaw.appsend.screen.upload.adapter.select_app.SelectAppItemBlueprint
import com.tomclaw.appsend.screen.upload.adapter.select_app.SelectAppItemPresenter
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
    private val info: PackageInfo?,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        interactor: UploadInteractor,
        adapterPresenter: Lazy<AdapterPresenter>,
        schedulers: SchedulersFactory
    ): UploadPresenter = UploadPresenterImpl(info, interactor, schedulers, state)

    @Provides
    @PerActivity
    internal fun provideInteractor(
        schedulers: SchedulersFactory
    ): UploadInteractor = UploadInteractorImpl(schedulers)

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
        locale: Locale,
        presenter: UploadPresenter
    ) = SelectAppItemPresenter(presenter)

}
