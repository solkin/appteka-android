package com.tomclaw.appsend.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.item.CommonItem;

import java.io.File;
import java.util.List;

/**
 * Created by ivsolkin on 27.01.17.
 */

public class IntentHelper {

    public static void openGooglePlay(Context context, String packageName) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (android.content.ActivityNotFoundException ex) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    public static void shareUrl(Context context, String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.send_url_to)));
    }

    private static void grantUriPermission(Context context, Uri uri, Intent intent) {
        if (isFileProviderUri()) {
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }

    public static void openFile(Context context, String filePath, String type) {
        File file = new File(filePath);
        Uri uri;
        if (isFileProviderUri()) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        grantUriPermission(context, uri, intent);

        context.startActivity(intent);
    }

    public static Intent openFileIntent(Context context, String filePath, String type) {
        File file = new File(filePath);
        Uri uri;
        if (isFileProviderUri()) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        grantUriPermission(context, uri, intent);

        return intent;
    }

    public static void shareApk(Context context, File file) {
        Uri uri;
        if (isFileProviderUri()) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, file.getName());
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("application/zip");

        grantUriPermission(context, uri, intent);

        context.startActivity(Intent.createChooser(intent, context.getResources().getText(R.string.send_to)));
    }

    public static void bluetoothApk(Context context, File file) {
        Uri uri;
        if (isFileProviderUri()) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, file.getName());
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType("application/zip");
        sendIntent.setPackage("com.android.bluetooth");
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.send_to)));
    }

    public static String formatText(Resources resources, String url, String label, long size) {
        String sizeString = FileHelper.formatBytes(resources, size);
        return resources.getString(R.string.uploaded_url, label, sizeString, url);
    }

    private static boolean isFileProviderUri() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }
}
