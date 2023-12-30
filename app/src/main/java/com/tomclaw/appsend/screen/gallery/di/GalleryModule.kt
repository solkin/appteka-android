package com.tomclaw.appsend.screen.gallery.di

import android.content.Context
import android.os.Bundle
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.screen.gallery.GalleryPresenter
import com.tomclaw.appsend.screen.gallery.GalleryPresenterImpl
import com.tomclaw.appsend.util.PerActivity
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Module
import dagger.Provides

@Module
class GalleryModule(
    private val context: Context,
    private val items: List<GalleryItem>,
    private val state: Bundle?
) {

    @Provides
    @PerActivity
    internal fun providePresenter(
        schedulers: SchedulersFactory
    ): GalleryPresenter = GalleryPresenterImpl(
        items,
        schedulers,
        state
    )

}