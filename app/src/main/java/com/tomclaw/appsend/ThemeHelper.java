package com.tomclaw.appsend;

import android.app.Activity;

/**
 * Created by ivsolkin on 21.09.16.
 */

public class ThemeHelper {

    public static boolean updateTheme(Activity activity) {
        boolean isDarkTheme = PreferenceHelper.isDarkTheme(activity);
        activity.setTheme(isDarkTheme ? R.style.AppThemeBlack : R.style.AppTheme);
        return isDarkTheme;
    }
}
