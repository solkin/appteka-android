package com.tomclaw.appsend.screen.store.di

import com.tomclaw.appsend.screen.store.StoreFragment
import com.tomclaw.appsend.util.PerFragment
import dagger.Subcomponent

@PerFragment
@Subcomponent(modules = [StoreModule::class])
interface StoreComponent {

    fun inject(fragment: StoreFragment)

}