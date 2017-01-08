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
        int color = ColorHelper.getAttributedColor(activity, R.attr.toolbar_background);
//        color = ColorHelper.darker(color, 0.6f);
        StatusBarUtil.setColor(activity, color);
        return isDarkTheme;
    }
}
