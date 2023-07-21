package com.tomclaw.appsend.screen.favorite.di

import com.tomclaw.appsend.screen.favorite.FavoriteActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [FavoriteModule::class])
interface FavoriteComponent {

    fun inject(activity: FavoriteActivity)

}
