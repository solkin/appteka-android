package com.tomclaw.appsend.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * Created by ivsolkin on 23.12.16.
 */
public class PackageIconGlideLoader implements ModelLoader<PackageInfo, InputStream> {

    private PackageManager packageManager;

    public PackageIconGlideLoader(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull final PackageInfo model,
                                               int width,
                                               int height,
                                               @NonNull Options options) {
        return new LoadData<>(new IconKey(model), new DataFetcher<InputStream>() {

            @Override
            public void loadData(@NonNull Priority priority,
                                 @NonNull DataCallback<? super InputStream> callback) {
                try {
                    byte[] data = PackageHelper.getPackageIconPng(
                            model.applicationInfo, packageManager
                    );
                    callback.onDataReady(new ByteArrayInputStream(data));
                } catch (Exception ex) {
                    callback.onLoadFailed(ex);
                }
            }

            @Override
            public void cleanup() {
            }

            @Override
            public void cancel() {
            }

            @NonNull
            @Override
            public Class<InputStream> getDataClass() {
                return InputStream.class;
            }

            @NonNull
            @Override
            public DataSource getDataSource() {
                return DataSource.LOCAL;
            }
        });
    }

    @Override
    public boolean handles(@NonNull PackageInfo packageInfo) {
        return true;
    }

    private class IconKey implements Key {

        private PackageInfo model;

        IconKey(PackageInfo model) {
            this.model = model;
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
            messageDigest.update(getId().getBytes());
        }

        public String getId() {
            return model.packageName + "-" + model.versionCode;
        }
    }
}
