package com.tomclaw.appsend;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by ivsolkin on 23.12.16.
 */
public class AppIconGlideLoader implements StreamModelLoader<AppInfo> {

    private PackageManager packageManager;

    public AppIconGlideLoader(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(final AppInfo model, int width, int height) {
        try {
            final PackageInfo packageInfo = packageManager.getPackageInfo(model.getPackageName(), 0);
            return new DataFetcher<InputStream>() {
                @Override
                public InputStream loadData(Priority priority) throws Exception {
                    Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
                    final Bitmap bitmap = drawableToBitmap(icon);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    return new ByteArrayInputStream(baos.toByteArray());
                }

                @Override
                public void cleanup() {
                }

                @Override
                public String getId() {
                    return packageInfo.packageName + "-" + packageInfo.versionCode;
                }

                @Override
                public void cancel() {
                }
            };
        } catch (Throwable ignored) {
        }
        return null;
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
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
}
