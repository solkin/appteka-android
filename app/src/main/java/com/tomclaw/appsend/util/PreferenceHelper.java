package com.tomclaw.appsend.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.tomclaw.appsend.R;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 11/15/13
 * Time: 1:56 PM
 */
public class PreferenceHelper {

    public static boolean isDarkTheme(Context context) {
        return getBooleanPreference(context, R.string.pref_dark_theme, R.bool.pref_dark_theme_default);
    }

    public static boolean isShowSystemApps(Context context) {
        return getBooleanPreference(context, R.string.pref_show_system, R.bool.pref_show_system_default);
    }

    public static boolean isRunnableOnly(Context context) {
        return getBooleanPreference(context, R.string.pref_runnable, R.bool.pref_runnable_default);
    }

    public static String getSortOrder(Context context) {
        return getStringPreference(context, R.string.pref_sort_order, R.string.pref_sort_order_default);
    }

    public static boolean isShowInstallCouch(Context context) {
        return getBooleanPreference(context, R.string.pref_install_couch, R.bool.pref_install_couch_default);
    }

    public static void setShowInstallCouch(Context context, boolean value) {
        setBooleanPreference(context, R.string.pref_install_couch, value);
    }

    public static boolean isShowResponsibilityDenial(Context context) {
        return getBooleanPreference(context, R.string.pref_responsibility_denial, R.bool.pref_responsibility_denial_default);
    }

    public static void setShowResponsibilityDenial(Context context, boolean value) {
        setBooleanPreference(context, R.string.pref_responsibility_denial, value);
    }

    public static long getCountTime(Context context) {
        return Long.parseLong(getStringPreference(context, R.string.pref_count_time, R.string.pref_count_time_default));
    }

    public static void setCountTime(Context context, long value) {
        setStringPreference(context, R.string.pref_count_time, String.valueOf(value));
    }

    private static boolean getBooleanPreference(Context context, int preferenceKey, int defaultValueKey) {
        return getSharedPreferences(context).getBoolean(context.getResources().getString(preferenceKey),
                context.getResources().getBoolean(defaultValueKey));
    }

    private static void setBooleanPreference(Context context, int preferenceKey, boolean value) {
        getSharedPreferences(context).edit().putBoolean(context.getResources().getString(preferenceKey),
                value).apply();
    }

    private static String getStringPreference(Context context, int preferenceKey, int defaultValueKey) {
        return getSharedPreferences(context).getString(context.getResources().getString(preferenceKey),
                context.getResources().getString(defaultValueKey));
    }

    private static void setStringPreference(Context context, int preferenceKey, String value) {
        getSharedPreferences(context).edit().putString(context.getResources().getString(preferenceKey),
                value).apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(getDefaultSharedPreferencesName(context),
                getSharedPreferencesMode());
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getSharedPreferencesMode() {
        return Context.MODE_PRIVATE;
    }
}
