package com.tomclaw.appsend.screen.search.di

import com.tomclaw.appsend.screen.search.SearchActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [SearchModule::class])
interface SearchComponent {

    fun inject(activity: SearchActivity)

}

