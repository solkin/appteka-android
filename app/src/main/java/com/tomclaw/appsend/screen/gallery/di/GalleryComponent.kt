package com.tomclaw.appsend.screen.gallery.di

import com.tomclaw.appsend.screen.gallery.GalleryActivity
import com.tomclaw.appsend.util.PerActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = [GalleryModule::class])
interface GalleryComponent {

    fun inject(activity: GalleryActivity)

}