package com.tomclaw.appsend.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

/**
 * Created by ivsolkin on 21.03.17.
 */
public class ManifestHelper {

    public static String getManifestString(Context context, String key) {
        return getBundle(context).getString(key);
    }

    private static Bundle getBundle(Context context) {
        Bundle bundle;
        try {
            bundle = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return bundle;
    }
}
