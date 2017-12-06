package com.tomclaw.appsend.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;

import java.lang.reflect.Method;

/**
 * Class to manage status and navigation bar tint effects when using KitKat
 * translucent system UI modes.
 */
@SuppressWarnings("WeakerAccess")
public class SystemBarTintManager {

    static {
        // Android allows a system property to override the presence of the navigation bar.
        // Used by the emulator.
        // See https://github.com/android/platform_frameworks_base/blob/master/policy/src/com/android/internal/policy/impl/PhoneWindowManager.java#L1076
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                @SuppressLint("PrivateApi") Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                navBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
                navBarOverride = null;
            }
        }
    }


    /**
     * The default system bar tint color value.
     */
    public static final int DEFAULT_TINT_COLOR = 0x99000000;

    private static String navBarOverride;

    private final SystemBarConfig config;
    private boolean isStatusBarAvailable;
    private boolean isNavBarAvailable;
    private boolean isStatusBarTintEnabled;
    private boolean mNavBarTintEnabled;
    private View statusBarTintView;
    private View navBarTintView;

    /**
     * Constructor. Call this in the host activity onCreate method after its
     * content view has been set. You should always create new instances when
     * the host activity is recreated.
     *
     * @param activity The host activity.
     */
    @TargetApi(24)
    @SuppressWarnings("ResourceType")
    public SystemBarTintManager(Activity activity) {

        Window win = activity.getWindow();
        ViewGroup decorViewGroup = (ViewGroup) win.getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // check theme attrs
            int[] attrs = {android.R.attr.windowTranslucentStatus,
                    android.R.attr.windowTranslucentNavigation};
            TypedArray a = activity.obtainStyledAttributes(attrs);
            try {
                isStatusBarAvailable = a.getBoolean(0, false);
                isNavBarAvailable = a.getBoolean(1, false);
            } finally {
                a.recycle();
            }

            // check window flags
            WindowManager.LayoutParams winParams = win.getAttributes();
            int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if ((winParams.flags & bits) != 0) {
                isStatusBarAvailable = true;
            }
            bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            if ((winParams.flags & bits) != 0) {
                isNavBarAvailable = true;
            }
        }

        config = new SystemBarConfig(activity, isStatusBarAvailable, isNavBarAvailable);
        // device might not have virtual navigation keys
        if (!config.hasNavigtionBar()) {
            isNavBarAvailable = false;
        }

        if (isStatusBarAvailable) {
            setupStatusBarView(activity, decorViewGroup);
        }
        if (isNavBarAvailable) {
            setupNavBarView(activity, decorViewGroup);
        }

    }

    /**
     * Enable tinting of the system status bar.
     * <p/>
     * If the platform is running Jelly Bean or earlier, or translucent system
     * UI modes have not been enabled in either the theme or via window flags,
     * then this method does nothing.
     *
     * @param enabled True to enable tinting, false to disable it (default).
     */
    public void setStatusBarTintEnabled(boolean enabled) {
        isStatusBarTintEnabled = enabled;
        if (isStatusBarAvailable) {
            statusBarTintView.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Enable tinting of the system navigation bar.
     * <p/>
     * If the platform does not have soft navigation keys, is running Jelly Bean
     * or earlier, or translucent system UI modes have not been enabled in either
     * the theme or via window flags, then this method does nothing.
     *
     * @param enabled True to enable tinting, false to disable it (default).
     */
    public void setNavigationBarTintEnabled(boolean enabled) {
        mNavBarTintEnabled = enabled;
        if (isNavBarAvailable) {
            navBarTintView.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Apply the specified color tint to all system UI bars.
     *
     * @param color The color of the background tint.
     */
    public void setTintColor(int color) {
        setStatusBarTintColor(color);
        setNavigationBarTintColor(color);
    }

    /**
     * Apply the specified drawable or color resource to all system UI bars.
     *
     * @param res The identifier of the resource.
     */
    public void setTintResource(int res) {
        setStatusBarTintResource(res);
        setNavigationBarTintResource(res);
    }

    /**
     * Apply the specified drawable to all system UI bars.
     *
     * @param drawable The drawable to use as the background, or null to remove it.
     */
    public void setTintDrawable(Drawable drawable) {
        setStatusBarTintDrawable(drawable);
        setNavigationBarTintDrawable(drawable);
    }

    /**
     * Apply the specified alpha to all system UI bars.
     *
     * @param alpha The alpha to use
     */
    public void setTintAlpha(float alpha) {
        setStatusBarAlpha(alpha);
        setNavigationBarAlpha(alpha);
    }

    /**
     * Apply the specified color tint to the system status bar.
     *
     * @param color The color of the background tint.
     */
    public void setStatusBarTintColor(int color) {
        if (isStatusBarAvailable) {
            statusBarTintView.setBackgroundColor(color);
        }
    }

    /**
     * Apply the specified drawable or color resource to the system status bar.
     *
     * @param res The identifier of the resource.
     */
    public void setStatusBarTintResource(int res) {
        if (isStatusBarAvailable) {
            statusBarTintView.setBackgroundResource(res);
        }
    }

    /**
     * Apply the specified drawable to the system status bar.
     *
     * @param drawable The drawable to use as the background, or null to remove it.
     */
    @SuppressWarnings("deprecation")
    public void setStatusBarTintDrawable(Drawable drawable) {
        if (isStatusBarAvailable) {
            statusBarTintView.setBackgroundDrawable(drawable);
        }
    }

    /**
     * Apply the specified alpha to the system status bar.
     *
     * @param alpha The alpha to use
     */
    @TargetApi(11)
    public void setStatusBarAlpha(float alpha) {
        if (isStatusBarAvailable) {
            statusBarTintView.setAlpha(alpha);
        }
    }

    /**
     * Apply the specified color tint to the system navigation bar.
     *
     * @param color The color of the background tint.
     */
    public void setNavigationBarTintColor(int color) {
        if (isNavBarAvailable) {
            navBarTintView.setBackgroundColor(color);
        }
    }

    /**
     * Apply the specified drawable or color resource to the system navigation bar.
     *
     * @param res The identifier of the resource.
     */
    public void setNavigationBarTintResource(int res) {
        if (isNavBarAvailable) {
            navBarTintView.setBackgroundResource(res);
        }
    }

    /**
     * Apply the specified drawable to the system navigation bar.
     *
     * @param drawable The drawable to use as the background, or null to remove it.
     */
    @SuppressWarnings("deprecation")
    public void setNavigationBarTintDrawable(Drawable drawable) {
        if (isNavBarAvailable) {
            navBarTintView.setBackgroundDrawable(drawable);
        }
    }

    /**
     * Apply the specified alpha to the system navigation bar.
     *
     * @param alpha The alpha to use
     */
    @TargetApi(11)
    public void setNavigationBarAlpha(float alpha) {
        if (isNavBarAvailable) {
            navBarTintView.setAlpha(alpha);
        }
    }

    /**
     * Get the system bar configuration.
     *
     * @return The system bar configuration for the current device configuration.
     */
    public SystemBarConfig getConfig() {
        return config;
    }

    /**
     * Is tinting enabled for the system status bar?
     *
     * @return True if enabled, False otherwise.
     */
    public boolean isStatusBarTintEnabled() {
        return isStatusBarTintEnabled;
    }

    /**
     * Is tinting enabled for the system navigation bar?
     *
     * @return True if enabled, False otherwise.
     */
    public boolean isNavBarTintEnabled() {
        return mNavBarTintEnabled;
    }

    private void setupStatusBarView(Context context, ViewGroup decorViewGroup) {
        statusBarTintView = new View(context);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, config.getStatusBarHeight());
        params.gravity = Gravity.TOP;
        if (isNavBarAvailable && !config.isNavigationAtBottom()) {
            params.rightMargin = config.getNavigationBarWidth();
        }
        statusBarTintView.setLayoutParams(params);
        statusBarTintView.setBackgroundColor(DEFAULT_TINT_COLOR);
        statusBarTintView.setVisibility(View.GONE);
        decorViewGroup.addView(statusBarTintView);
    }

    @SuppressLint("RtlHardcoded")
    private void setupNavBarView(Context context, ViewGroup decorViewGroup) {
        navBarTintView = new View(context);
        LayoutParams params;
        if (config.isNavigationAtBottom()) {
            params = new LayoutParams(LayoutParams.MATCH_PARENT, config.getNavigationBarHeight());
            params.gravity = Gravity.BOTTOM;
        } else {
            params = new LayoutParams(config.getNavigationBarWidth(), LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.RIGHT;
        }
        navBarTintView.setLayoutParams(params);
        navBarTintView.setBackgroundColor(DEFAULT_TINT_COLOR);
        navBarTintView.setVisibility(View.GONE);
        decorViewGroup.addView(navBarTintView);
    }

    /**
     * Class which describes system bar sizing and other characteristics for the current
     * device configuration.
     */
    public static class SystemBarConfig {

        private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
        private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
        private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";
        private static final String NAV_BAR_WIDTH_RES_NAME = "navigation_bar_width";
        private static final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";

        private final boolean mTranslucentStatusBar;
        private final boolean mTranslucentNavBar;
        private final int mStatusBarHeight;
        private final int mActionBarHeight;
        private final boolean mHasNavigationBar;
        private final int mNavigationBarHeight;
        private final int mNavigationBarWidth;
        private final boolean mInPortrait;
        private final float mSmallestWidthDp;

        private SystemBarConfig(Activity activity, boolean translucentStatusBar, boolean traslucentNavBar) {
            Resources res = activity.getResources();
            mInPortrait = (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
            mSmallestWidthDp = getSmallestWidthDp(activity);
            mStatusBarHeight = getInternalDimensionSize(res, STATUS_BAR_HEIGHT_RES_NAME);
            mActionBarHeight = getActionBarHeight(activity);
            mNavigationBarHeight = getNavigationBarHeight(activity);
            mNavigationBarWidth = getNavigationBarWidth(activity);
            mHasNavigationBar = (mNavigationBarHeight > 0);
            mTranslucentStatusBar = translucentStatusBar;
            mTranslucentNavBar = traslucentNavBar;
        }

        @TargetApi(14)
        private int getActionBarHeight(Context context) {
            TypedValue tv = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }

        @TargetApi(14)
        private int getNavigationBarHeight(Context context) {
            Resources res = context.getResources();
            int result = 0;
            if (hasNavBar(context)) {
                String key;
                if (mInPortrait) {
                    key = NAV_BAR_HEIGHT_RES_NAME;
                } else {
                    key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME;
                }
                return getInternalDimensionSize(res, key);
            }
            return result;
        }

        @TargetApi(14)
        private int getNavigationBarWidth(Context context) {
            Resources res = context.getResources();
            int result = 0;
            if (hasNavBar(context)) {
                return getInternalDimensionSize(res, NAV_BAR_WIDTH_RES_NAME);
            }
            return result;
        }

        @TargetApi(14)
        private boolean hasNavBar(Context context) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier(SHOW_NAV_BAR_RES_NAME, "bool", "android");
            if (resourceId != 0) {
                boolean hasNav = res.getBoolean(resourceId);
                // check override flag (see static block)
                if ("1".equals(navBarOverride)) {
                    hasNav = false;
                } else if ("0".equals(navBarOverride)) {
                    hasNav = true;
                }
                return hasNav;
            } else { // fallback
                return !ViewConfiguration.get(context).hasPermanentMenuKey();
            }
        }

        private int getInternalDimensionSize(Resources res, String key) {
            int result = 0;
            int resourceId = res.getIdentifier(key, "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
            return result;
        }

        @SuppressLint("NewApi")
        private float getSmallestWidthDp(Activity activity) {
            DisplayMetrics metrics = new DisplayMetrics();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            } else {
                // TODO this is not correct, but we don't really care pre-kitkat
                activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            }
            float widthDp = metrics.widthPixels / metrics.density;
            float heightDp = metrics.heightPixels / metrics.density;
            return Math.min(widthDp, heightDp);
        }

        /**
         * Should a navigation bar appear at the bottom of the screen in the current
         * device configuration? A navigation bar may appear on the right side of
         * the screen in certain configurations.
         *
         * @return True if navigation should appear at the bottom of the screen, False otherwise.
         */
        public boolean isNavigationAtBottom() {
            return (mSmallestWidthDp >= 600 || mInPortrait);
        }

        /**
         * Get the height of the system status bar.
         *
         * @return The height of the status bar (in pixels).
         */
        public int getStatusBarHeight() {
            return mStatusBarHeight;
        }

        /**
         * Get the height of the action bar.
         *
         * @return The height of the action bar (in pixels).
         */
        public int getActionBarHeight() {
            return mActionBarHeight;
        }

        /**
         * Does this device have a system navigation bar?
         *
         * @return True if this device uses soft key navigation, False otherwise.
         */
        public boolean hasNavigtionBar() {
            return mHasNavigationBar;
        }

        /**
         * Get the height of the system navigation bar.
         *
         * @return The height of the navigation bar (in pixels). If the device does not have
         * soft navigation keys, this will always return 0.
         */
        public int getNavigationBarHeight() {
            return mNavigationBarHeight;
        }

        /**
         * Get the width of the system navigation bar when it is placed vertically on the screen.
         *
         * @return The width of the navigation bar (in pixels). If the device does not have
         * soft navigation keys, this will always return 0.
         */
        public int getNavigationBarWidth() {
            return mNavigationBarWidth;
        }

        /**
         * Get the layout inset for any system UI that appears at the top of the screen.
         *
         * @param withActionBar True to include the height of the action bar, False otherwise.
         * @return The layout inset (in pixels).
         */
        public int getPixelInsetTop(boolean withActionBar) {
            return (mTranslucentStatusBar ? mStatusBarHeight : 0) + (withActionBar ? mActionBarHeight : 0);
        }

        /**
         * Get the layout inset for any system UI that appears at the bottom of the screen.
         *
         * @return The layout inset (in pixels).
         */
        public int getPixelInsetBottom() {
            if (mTranslucentNavBar && isNavigationAtBottom()) {
                return mNavigationBarHeight;
            } else {
                return 0;
            }
        }

        /**
         * Get the layout inset for any system UI that appears at the right of the screen.
         *
         * @return The layout inset (in pixels).
         */
        public int getPixelInsetRight() {
            if (mTranslucentNavBar && !isNavigationAtBottom()) {
                return mNavigationBarWidth;
            } else {
                return 0;
            }
        }

    }

}