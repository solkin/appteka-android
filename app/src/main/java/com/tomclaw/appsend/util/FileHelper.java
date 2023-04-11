package com.tomclaw.appsend.util;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Environment;
import android.text.TextUtils;

import com.tomclaw.appsend.R;

import java.io.File;

/**
 * Created by Solkin on 18.10.2014.
 */
public class FileHelper {

    private static final String[] RESERVED_CHARS = {"|", "\\", "/", "?", "*", "<", "\"", ":", ">"};

    public static String getFileExtensionFromPath(String path) {
        String suffix = "";
        if (!TextUtils.isEmpty(path)) {
            int index = path.lastIndexOf(".");
            if (index != -1) {
                suffix = path.substring(index + 1);
            }
        }
        return suffix;
    }

    public static String formatBytes(Resources resources, long bytes) {
        if (bytes < 1024) {
            return resources.getString(R.string.bytes, bytes);
        } else if (bytes < 1024 * 1024) {
            return resources.getString(R.string.kibibytes, bytes / 1024.0f);
        } else if (bytes < 1024 * 1024 * 1024) {
            return resources.getString(R.string.mibibytes, bytes / 1024.0f / 1024.0f);
        } else {
            return resources.getString(R.string.gigibytes, bytes / 1024.0f / 1024.0f / 1024.0f);
        }
    }

    public static String escapeFileSymbols(String name) {
        for (String symbol : RESERVED_CHARS) {
            name = name.replace(symbol.charAt(0), '_');
        }
        return name;
    }

    @SuppressLint("NewApi")
    public static File getExternalDirectory() {
        File externalDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File directory = new File(externalDirectory, "Apps");
        directory.mkdirs();
        return directory;
    }
}
