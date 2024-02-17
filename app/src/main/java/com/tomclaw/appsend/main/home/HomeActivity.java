package com.tomclaw.appsend.main.home;

import static com.microsoft.appcenter.analytics.Analytics.trackEvent;
import static com.tomclaw.appsend.Appteka.getLastRunBuildNumber;
import static com.tomclaw.appsend.Appteka.wasRegistered;
import static com.tomclaw.appsend.screen.details.DetailsActivityKt.createDetailsActivityIntent;
import static com.tomclaw.appsend.screen.profile.ProfileActivityKt.createProfileActivityIntent;
import static com.tomclaw.appsend.screen.upload.UploadActivityKt.createUploadActivityIntent;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.greysonparrelli.permiso.PermisoActivity;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.main.about.AboutActivity;
import com.tomclaw.appsend.main.controller.UpdateController;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.local.DistroActivity_;
import com.tomclaw.appsend.main.local.InstalledActivity_;
import com.tomclaw.appsend.main.migrate.MigrateActivity_;
import com.tomclaw.appsend.main.profile.ProfileFragment_;
import com.tomclaw.appsend.main.settings.SettingsActivity_;
import com.tomclaw.appsend.main.store.search.SearchActivity_;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.net.Session_;
import com.tomclaw.appsend.net.UserData;
import com.tomclaw.appsend.net.UserDataListener;
import com.tomclaw.appsend.screen.store.StoreFragment;
import com.tomclaw.appsend.screen.topics.TopicsFragment;
import com.tomclaw.appsend.util.LocaleHelper;
import com.tomclaw.appsend.util.PreferenceHelper;
import com.tomclaw.appsend.util.ThemeHelper;

public class HomeActivity extends PermisoActivity implements UserDataListener,
        UpdateController.UpdateCallback, UnreadCheckTask.UnreadListener {

    public static final String APP_IDENTIFIER_KEY = "appcenter.app_identifier";

    public static final String ACTION_STORE = "com.tomclaw.appsend.cloud";
    public static final String ACTION_DISCUSS = "com.tomclaw.appsend.discuss";
    public static final String ACTION_APPS = "com.tomclaw.appsend.apps";
    public static final String ACTION_INSTALL = "com.tomclaw.appsend.install";

    private static final int NAV_STORE = 0;
    private static final int NAV_DISCUSS = 1;
    private static final int NAV_PROFILE = 2;

    private View updateBlock;
    private FloatingActionButton fab;
    private boolean isDarkTheme;
    private BottomNavigationView bottomNavigation;

    public static int navItemIndex = 0;

    private static final String TAG_STORE = "store";
    private static final String TAG_DISCUSS = "discuss";
    private static final String TAG_PROFILE = "profile";
    public static String CURRENT_TAG = TAG_STORE;

    private String[] activityTitles;

    private final static boolean shouldLoadHomeFragOnBackPress = true;
    private Handler handler;

    private @Nullable
    UnreadCheckTask unreadCheckTask;

    private final HomeFragment[] homeFragments = new HomeFragment[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isCreateInstance = savedInstanceState == null;

        isDarkTheme = ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String intentAction = getIntent().getAction();

        handler = new Handler();

        fab = findViewById(R.id.fab);

        updateBlock = findViewById(R.id.update_block);
        findViewById(R.id.update_later).setOnClickListener(v -> onUpdateLater());
        findViewById(R.id.update).setOnClickListener(v -> onUpdate());

        bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.setOnItemSelectedListener((item) -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_store) {
                navItemIndex = NAV_STORE;
                CURRENT_TAG = TAG_STORE;
                trackEvent("click-tab-store");
            } else if (itemId == R.id.nav_discuss) {
                navItemIndex = NAV_DISCUSS;
                CURRENT_TAG = TAG_DISCUSS;
                if (unreadCheckTask != null) {
                    unreadCheckTask.resetUnreadCount();
                }
                trackEvent("click-tab-discuss");
            } else if (itemId == R.id.nav_profile) {
                navItemIndex = NAV_PROFILE;
                CURRENT_TAG = TAG_PROFILE;
                trackEvent("click-tab-profile");
            }
            loadHomeFragment();
            return true;
        });

        activityTitles = new String[]{
                getString(R.string.nav_store),
                getString(R.string.nav_discuss),
                getString(R.string.tab_profile)
        };

        fab.setOnClickListener(view -> {
            Intent intent = createUploadActivityIntent(HomeActivity.this, null, null, null);
            startActivity(intent);
        });

        setToolbarTitle();

        if (isCreateInstance) {
            if (!setNavByAction(intentAction)) {
                navItemIndex = NAV_STORE;
                CURRENT_TAG = TAG_STORE;
            }
            loadHomeFragment();
        } else {
            toggleFab();
        }

        if (isCreateInstance) {
            checkForUpdates();
        }

        register(getApplication());

        checkMigration();

        startActivity(createProfileActivityIntent(this, 1));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Session.getInstance().getUserHolder().attachListener(this);
        UpdateController.getInstance().onAttach(this);
        TaskExecutor.getInstance().execute(new StatusCheckTask(this));
        UnreadCheckTask unreadCheckTask = new UnreadCheckTask(Session_.getInstance().getUserData().getGuid());
        this.unreadCheckTask = unreadCheckTask;
        unreadCheckTask.setListener(this);
        TaskExecutor.getInstance().execute(unreadCheckTask);
    }

    @Override
    protected void onStop() {
        Session.getInstance().getUserHolder().removeListener(this);
        UpdateController.getInstance().onDetach(this);
        if (unreadCheckTask != null) {
            unreadCheckTask.detachListener();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDarkTheme != PreferenceHelper.isDarkTheme(this)) {
            Intent intent = getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            startActivity(intent);
        }
    }

    @Override
    public void onUserDataChanged(@NonNull final UserData userData) {
    }

    private void updateUnreadIndicator(int count) {
        if (count > 0 && navItemIndex == NAV_DISCUSS) {
            if (unreadCheckTask != null) {
                unreadCheckTask.resetUnreadCount();
            }
            count = 0;
        }
        if (count > 0) {
            BadgeDrawable badge = bottomNavigation.getOrCreateBadge(R.id.nav_discuss);
            badge.setVisible(true);
            badge.setNumber(count);
        } else {
            BadgeDrawable badge = bottomNavigation.getBadge(R.id.nav_discuss);
            if (badge != null) {
                badge.setVisible(false);
                badge.clearNumber();
            }
        }
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        setToolbarTitle();

        HomeFragment currentFragment = getHomeFragment();
        if (currentFragment != null) {
            toggleFab();
            return;
        }

        Runnable pendingRunnable = () -> {
            Fragment fragment = createHomeFragment();

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(0, 0);
            fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
            fragmentTransaction.commitAllowingStateLoss();

            invalidateOptionsMenu();

            toggleFab();
        };

        handler.post(pendingRunnable);
    }

    @Nullable
    private HomeFragment getHomeFragment() {
        return (HomeFragment) getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
    }

    private HomeFragment createHomeFragment() {
        switch (navItemIndex) {
            case NAV_STORE -> {
                bottomNavigation.setSelectedItemId(R.id.nav_store);
                if (homeFragments[navItemIndex] == null) {
                    homeFragments[navItemIndex] = new StoreFragment();
                }
            }
            case NAV_DISCUSS -> {
                bottomNavigation.setSelectedItemId(R.id.nav_discuss);
                if (homeFragments[navItemIndex] == null) {
                    homeFragments[navItemIndex] = new TopicsFragment();
                }
            }
            case NAV_PROFILE -> {
                bottomNavigation.setSelectedItemId(R.id.nav_profile);
                if (homeFragments[navItemIndex] == null) {
                    homeFragments[navItemIndex] = ProfileFragment_.builder().userId(0L).build();
                }
            }
            default -> throw new IllegalStateException("Invalid navigation item index");
        }
        return homeFragments[navItemIndex];
    }

    private boolean setNavByAction(String action) {
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case ACTION_STORE:
                    navItemIndex = NAV_STORE;
                    CURRENT_TAG = TAG_STORE;
                    return true;
                case ACTION_DISCUSS:
                    navItemIndex = NAV_DISCUSS;
                    CURRENT_TAG = TAG_DISCUSS;
                    return true;
                case ACTION_APPS:
                    InstalledActivity_.intent(this).start();
                    return true;
                case ACTION_INSTALL:
                    DistroActivity_.intent(this).start();
                    return true;
            }
        }
        return false;
    }

    private void setToolbarTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(activityTitles[navItemIndex]);
        }
    }

    private boolean onMenuSelected(int id) {
        if (id == R.id.menu_search) {
            SearchActivity_.intent(this).start();
            return true;
        } else if (id == R.id.nav_settings) {
            SettingsActivity_.intent(HomeActivity.this).start();
            return true;
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        throw new IllegalStateException("Invalid menu id");
    }

    @Override
    public void onBackPressed() {
        if (shouldLoadHomeFragOnBackPress) {
            if (navItemIndex != NAV_STORE) {
                navItemIndex = NAV_STORE;
                CURRENT_TAG = TAG_STORE;
                loadHomeFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuRes = navItemIndex == NAV_STORE ? R.menu.store_menu : R.menu.home_menu;
        getMenuInflater().inflate(menuRes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onMenuSelected(item.getItemId());
    }

    private void toggleFab() {
        if (navItemIndex == NAV_STORE) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    private void checkForUpdates() {
        UpdateController.getInstance().load(this);
    }

    @Override
    public void onUpdateAvailable(StoreItem item) {
        updateBlock.setVisibility(View.VISIBLE);
    }

    private void onUpdateLater() {
        UpdateController.getInstance().resetUpdateFlag();
        updateBlock.setVisibility(View.GONE);
    }

    private void onUpdate() {
        StoreItem item = UpdateController.getInstance().getStoreItem();
        if (item != null) {
            String appId = item.getAppId();
            String label = LocaleHelper.getLocalizedLabel(item);
            Intent intent = createDetailsActivityIntent(
                    this,
                    appId,
                    null,
                    label,
                    false,
                    true
            );

            startActivity(intent);
        }
    }

    @Override
    public void onUnread(int count) {
        updateUnreadIndicator(count);
    }

    private void checkMigration() {
        if (wasRegistered() && getLastRunBuildNumber() == 0) {
            MigrateActivity_.intent(this).start();
        }
    }

    private void register(Application application) {
        String appIdentifier = getAppIdentifier(application.getApplicationContext());
        AppCenter.start(getApplication(), appIdentifier, Analytics.class, Crashes.class);
    }

    private String getAppIdentifier(Context context) {
        String appIdentifier = getManifestString(context, APP_IDENTIFIER_KEY);
        if (TextUtils.isEmpty(appIdentifier)) {
            throw new RuntimeException("AppCenter app identifier was not configured correctly in manifest or build configuration.");
        }
        return appIdentifier;
    }

    private String getManifestString(Context context, String key) {
        return getManifestBundle(context).getString(key);
    }

    private Bundle getManifestBundle(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA
            ).metaData;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Intent createStoreActivityIntent(Context context) {
        return new Intent(context, HomeActivity.class)
                .setAction(HomeActivity.ACTION_STORE)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

}
