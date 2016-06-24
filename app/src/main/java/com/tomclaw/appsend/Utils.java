package com.tomclaw.appsend;

import android.app.Activity;
import android.os.Build;

/**
 * Created by Solkin on 13.01.2015.
 */
public class Utils {

    public static void setupTint(Activity activity) {
        setupTint(activity, activity.getResources().getColor(R.color.action_bar_color));
    }

    public static void setupTint(Activity activity, int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(color);
    }
}
