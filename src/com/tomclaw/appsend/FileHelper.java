package com.tomclaw.appsend;

import android.content.res.Resources;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import com.tomclaw.shareapp.R;

/**
 * Created by Solkin on 18.10.2014.
 */
public class FileHelper {

    private static final String[] RESERVED_CHARS = {"|", "\\", "?", "*", "<", "\"", ":", ">"};

    public static String getMimeType(String path) {
        String type = null;
        String extension = getFileExtensionFromPath(path);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension.toLowerCase());
        }
        if (TextUtils.isEmpty(type)) {
            type = "application/octet-stream";
        }
        return type;
    }

    public static String getFileBaseFromName(String name) {
        String base = name;
        if (!TextUtils.isEmpty(name)) {
            int index = name.lastIndexOf(".");
            if (index != -1) {
                base = name.substring(0, index);
            }
        }
        return base;
    }

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

    public static String formatSpeed(float bytesPerSecond) {
        float bitsPerSecond = bytesPerSecond * 8;
        int unit = 1000;
        if (bitsPerSecond < unit) return bitsPerSecond + " bits/sec";
        int exp = (int) (Math.log(bitsPerSecond) / Math.log(unit));
        String pre = String.valueOf("kmgtpe".charAt(exp - 1));
        return String.format("%.1f %sB/sec", bitsPerSecond / Math.pow(unit, exp), pre);
    }

    public static String escapeFileSymbols(String name) {
        for(String symbol : RESERVED_CHARS) {
            name = name.replace(symbol.charAt(0), '_');
        }
        return name;
    }
}
