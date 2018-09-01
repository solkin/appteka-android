package com.tomclaw.appsend.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.tomclaw.appsend.main.item.StoreItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by solkin on 23.04.17.
 */
public class PackageHelper {

    public static int getInstalledVersionCode(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getPackageInfo(packageName, 0).versionCode;
        } catch (Throwable ex) {
            return StoreItem.NOT_INSTALLED;
        }
    }

    public static byte[] getPackageIconPng(ApplicationInfo info,
                                           PackageManager packageManager) {
        Drawable icon = info.loadIcon(packageManager);
        final Bitmap bitmap = drawableToBitmap(icon);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException ignored) {
        }
        return data;
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
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
