package com.tomclaw.appsend.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    public LoadData<InputStream> buildLoadData(final PackageInfo model,
                                               int width,
                                               int height,
                                               Options options) {
        return new LoadData<>(new IconKey(model), new DataFetcher<InputStream>() {

            @Override
            public void loadData(Priority priority, DataCallback<? super InputStream> callback) {
                try {
                    Drawable icon = model.applicationInfo.loadIcon(packageManager);
                    final Bitmap bitmap = drawableToBitmap(icon);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    callback.onDataReady(new ByteArrayInputStream(baos.toByteArray()));
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

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public boolean handles(PackageInfo packageInfo) {
        return true;
    }

    private class IconKey implements Key {

        private PackageInfo model;

        IconKey(PackageInfo model) {
            this.model = model;
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update(getId().getBytes());
        }

        public String getId() {
            return model.packageName + "-" + model.versionCode;
        }
    }
}
