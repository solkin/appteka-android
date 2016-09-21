package com.tomclaw.appsend;

import android.content.Context;
import android.content.SharedPreferences;

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

    private static boolean getBooleanPreference(Context context, int preferenceKey, int defaultValueKey) {
        return getSharedPreferences(context).getBoolean(context.getResources().getString(preferenceKey),
                context.getResources().getBoolean(defaultValueKey));
    }

    private static void setBooleanPreference(Context context, int preferenceKey, boolean value) {
        getSharedPreferences(context).edit().putBoolean(context.getResources().getString(preferenceKey),
                value).commit();
    }

    private static String getStringPreference(Context context, int preferenceKey, int defaultValueKey) {
        return getSharedPreferences(context).getString(context.getResources().getString(preferenceKey),
                context.getResources().getString(defaultValueKey));
    }

    private static void setStringPreference(Context context, int preferenceKey, String value) {
        getSharedPreferences(context).edit().putString(context.getResources().getString(preferenceKey),
                value).commit();
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(getDefaultSharedPreferencesName(context),
                getSharedPreferencesMode());
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getSharedPreferencesMode() {
        return Context.MODE_MULTI_PROCESS;
    }
}
