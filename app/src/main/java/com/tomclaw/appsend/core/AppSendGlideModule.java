package com.tomclaw.appsend.core;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.tomclaw.appsend.util.PackageIconGlideLoader;

import java.io.InputStream;

/**
 * Created by solkin on 21/01/2018.
 */
@GlideModule
public class AppSendGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(final Context context, Glide glide, Registry registry) {
        registry.append(PackageInfo.class, InputStream.class, new ModelLoaderFactory<PackageInfo, InputStream>() {
            @Override
            public ModelLoader<PackageInfo, InputStream> build(MultiModelLoaderFactory multiFactory) {
                return new PackageIconGlideLoader(context.getPackageManager());
            }

            @Override
            public void teardown() {
            }
        });
    }
}
