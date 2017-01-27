package com.tomclaw.appsend.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.item.CommonItem;

import java.io.File;

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

    public static void shareApk(Context context, File destination) {
        Uri uri = Uri.fromFile(destination);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, destination.getName());
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType("application/zip");
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.send_to)));
    }

    public static void bluetoothApk(Context context, CommonItem item) {
        Uri uri = Uri.fromFile(new File(item.getPath()));
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, item.getLabel());
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType("application/zip");
        sendIntent.setPackage("com.android.bluetooth");
        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.send_to)));
    }

    public static String formatText(Resources resources, String url, String label, long size) {
        String sizeString = FileHelper.formatBytes(resources, size);
        return resources.getString(R.string.uploaded_url, label, sizeString, url);
    }
}
