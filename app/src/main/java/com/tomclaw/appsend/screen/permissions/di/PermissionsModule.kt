package com.tomclaw.appsend.screen.permissions.di

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.screen.permissions.PermissionInfoProvider
import com.tomclaw.appsend.screen.permissions.PermissionInfoProviderImpl
import com.tomclaw.appsend.screen.permissions.PermissionsConverter
import com.tomclaw.appsend.screen.permissions.PermissionsConverterImpl
import com.tomclaw.appsend.screen.permissions.PermissionsPresenter
import com.tomclaw.appsend.screen.permissions.PermissionsPresenterImpl
import com.tomclaw.appsend.screen.permissions.adapter.safe.SafePermissionItemBlueprint
import com.tomclaw.appsend.screen.permissions.adapter.safe.SafePermissionItemPresenter
import com.tomclaw.appsend.screen.permissions.adapter.unsafe.UnsafePermissionItemBlueprint
import com.tomclaw.appsend.screen.permissions.adapter.unsafe.UnsafePermissionItemPresenter
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
class PermissionsModule(
    private val context: Context,
    private val permissions: List<String>,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        adapterPresenter: Lazy<AdapterPresenter>,
        converter: PermissionsConverter,
        schedulers: SchedulersFactory
    ): PermissionsPresenter =
        PermissionsPresenterImpl(permissions, adapterPresenter, converter, schedulers, state)

    @Provides
    @PerActivity
    internal fun providePermissionInfoProvider(packageManager: PackageManager): PermissionInfoProvider {
        return PermissionInfoProviderImpl(packageManager)
    }

    @Provides
    @PerActivity
    internal fun providePermissionsConverter(
        permissionInfoProvider: PermissionInfoProvider
    ): PermissionsConverter {
        return PermissionsConverterImpl(permissionInfoProvider)
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
    internal fun provideSafeItemBlueprint(
        presenter: SafePermissionItemPresenter
    ): ItemBlueprint<*, *> = SafePermissionItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideSafePermissionItemPresenter() = SafePermissionItemPresenter()

    @Provides
    @IntoSet
    @PerActivity
    internal fun provideUnsafeItemBlueprint(
        presenter: UnsafePermissionItemPresenter
    ): ItemBlueprint<*, *> = UnsafePermissionItemBlueprint(presenter)

    @Provides
    @PerActivity
    internal fun provideUnsafePermissionItemPresenter() = UnsafePermissionItemPresenter()

}
