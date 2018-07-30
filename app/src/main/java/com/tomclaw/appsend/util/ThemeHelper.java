package com.tomclaw.appsend.util;

import android.app.Activity;

import com.jaeger.library.StatusBarUtil;
import com.tomclaw.appsend.R;

/**
 * Created by ivsolkin on 21.09.16.
 */
public class ThemeHelper {

    public static boolean updateTheme(Activity activity) {
        boolean isDarkTheme = PreferenceHelper.isDarkTheme(activity);
        activity.setTheme(isDarkTheme ? R.style.AppThemeBlack : R.style.AppTheme);
        return isDarkTheme;
    }

    public static void updateStatusBar(Activity activity) {
        int toolbarColor = ColorHelper.getAttributedColor(activity, R.attr.toolbar_background);
        updateStatusBar(activity, toolbarColor);
    }

    public static void updateStatusBar(Activity activity, int color) {
        StatusBarUtil.setColor(activity, color);
    }

}
