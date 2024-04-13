package com.tomclaw.appsend.util;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Intent;

import com.tomclaw.appsend.R;

import org.jetbrains.annotations.NotNull;

/**
 * Created by ivsolkin on 21.09.16.
 */
public class ThemeHelper {

    public static boolean updateTheme(Activity activity) {
        boolean isDarkTheme = PreferenceHelper.isDarkTheme(activity);
        activity.setTheme(isDarkTheme ? R.style.AppThemeBlack : R.style.AppTheme);
        return isDarkTheme;
    }

    public static void restartIfThemeChanged(boolean isDarkTheme, @NotNull Activity activity) {
        if (isDarkTheme != PreferenceHelper.isDarkTheme(activity)) {
            Intent intent = activity.getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.finish();
            startActivity(activity, intent, null);
        }
    }
}
