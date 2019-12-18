package com.tomclaw.appsend.main.home;

import android.content.Intent;
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

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.greysonparrelli.permiso.PermisoActivity;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.about.AboutActivity;
import com.tomclaw.appsend.main.controller.DiscussController;
import com.tomclaw.appsend.main.controller.UpdateController;
import com.tomclaw.appsend.main.discuss.DiscussFragment_;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.local.DialogData;
import com.tomclaw.appsend.main.local.SelectLocalAppActivity;
import com.tomclaw.appsend.main.local.SelectLocalAppActivity_;
import com.tomclaw.appsend.main.migrate.MigrateActivity_;
import com.tomclaw.appsend.main.profile.ProfileFragment_;
import com.tomclaw.appsend.main.settings.SettingsActivity_;
import com.tomclaw.appsend.main.store.StoreFragment_;
import com.tomclaw.appsend.main.store.UserUploadsFragment_;
import com.tomclaw.appsend.main.store.search.SearchActivity_;
import com.tomclaw.appsend.main.upload.UploadActivity;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.net.UserData;
import com.tomclaw.appsend.net.UserDataListener;
import com.tomclaw.appsend.util.ColorHelper;
import com.tomclaw.appsend.util.LocaleHelper;
import com.tomclaw.appsend.util.PreferenceHelper;
import com.tomclaw.appsend.util.ThemeHelper;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.metrics.MetricsManager;

import static com.tomclaw.appsend.AppSend.getLastRunBuildNumber;
import static com.tomclaw.appsend.AppSend.wasRegistered;

public class HomeActivity extends PermisoActivity implements UserDataListener,
        UpdateController.UpdateCallback,
        DiscussController.DiscussCallback {

    public static final String ACTION_STORE = "com.tomclaw.appsend.cloud";
    public static final String ACTION_DISCUSS = "com.tomclaw.appsend.discuss";

    private static final int NAV_STORE = 0;
    private static final int NAV_UPLOADS = 1;
    private static final int NAV_DISCUSS = 2;
    private static final int NAV_PROFILE = 5;

    private static final int REQUEST_UPLOAD = 4;

    private View updateBlock;
    private FloatingActionButton fab;
    private boolean isDarkTheme;
    private AHBottomNavigation bottomNavigation;

    public static int navItemIndex = 0;

    private static final String TAG_STORE = "store";
    private static final String TAG_UPLOADS = "uploads";
    private static final String TAG_DISCUSS = "discuss";
    private static final String TAG_PROFILE = "profile";
    public static String CURRENT_TAG = TAG_STORE;

    private String[] activityTitles;

    private final static boolean shouldLoadHomeFragOnBackPress = true;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isCreateInstance = savedInstanceState == null;

        isDarkTheme = ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ThemeHelper.updateStatusBar(this);

        String intentAction = getIntent().getAction();

        handler = new Handler();

        fab = findViewById(R.id.fab);

        updateBlock = findViewById(R.id.update_block);
        findViewById(R.id.update_later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpdateLater();
            }
        });
        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpdate();
            }
        });

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.addItem(new AHBottomNavigationItem(getString(R.string.tab_store), R.drawable.ic_store));
        bottomNavigation.addItem(new AHBottomNavigationItem(getString(R.string.tab_discuss), R.drawable.ic_discuss));
        bottomNavigation.addItem(new AHBottomNavigationItem(getString(R.string.tab_profile), R.drawable.ic_account));

        bottomNavigation.setDefaultBackgroundColor(ColorHelper.getAttributedColor(this, R.attr.bottom_bar_background));
        bottomNavigation.setAccentColor(getResources().getColor(R.color.accent_color));
        bottomNavigation.setInactiveColor(getResources().getColor(R.color.grey_dark));
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setForceTint(true);
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.manageFloatingActionButtonBehavior(fab);
        bottomNavigation.setTitleTextSize(
                getResources().getDimension(R.dimen.bottom_navigation_text_size_active),
                getResources().getDimension(R.dimen.bottom_navigation_text_size_inactive)
        );
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch (position) {
                    case 0:
                        navItemIndex = NAV_STORE;
                        CURRENT_TAG = TAG_STORE;
                        break;
                    case 1:
                        navItemIndex = NAV_DISCUSS;
                        CURRENT_TAG = TAG_DISCUSS;
                        break;
                    case 2:
                        navItemIndex = NAV_PROFILE;
                        CURRENT_TAG = TAG_PROFILE;
                        break;
                }
                loadHomeFragment();
                return true;
            }
        });

        activityTitles = new String[]{
                getString(R.string.nav_store),
                getString(R.string.nav_uploads),
                getString(R.string.nav_discuss),
                getString(R.string.nav_installed),
                getString(R.string.nav_distro),
                getString(R.string.tab_profile)
        };

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogData dialogData = new DialogData(getString(R.string.upload_app_title), getString(R.string.upload_app_message));
                SelectLocalAppActivity_.intent(HomeActivity.this).dialogData(dialogData).startForResult(REQUEST_UPLOAD);
            }
        });

        setToolbarTitle();

        if (isCreateInstance) {
            if (!setNavByAction(intentAction)) {
                navItemIndex = NAV_STORE;
                CURRENT_TAG = TAG_STORE;
            }
            loadHomeFragment();
        }

        if (isCreateInstance) {
            checkForUpdates();
        }

        checkForCrashes();
        MetricsManager.register(getApplication());

        checkMigration();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Session.getInstance().getUserHolder().attachListener(this);
        UpdateController.getInstance().onAttach(this);
        DiscussController.getInstance().onAttach(this);
    }

    @Override
    protected void onStop() {
        Session.getInstance().getUserHolder().removeListener(this);
        UpdateController.getInstance().onDetach(this);
        DiscussController.getInstance().onDetach(this);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_UPLOAD: {
                if (resultCode == RESULT_OK) {
                    CommonItem item = data.getParcelableExtra(SelectLocalAppActivity.SELECTED_ITEM);
                    Intent intent = new Intent(this, UploadActivity.class);
                    intent.putExtra(UploadActivity.UPLOAD_ITEM, item);
                    startActivity(intent);
                }
                break;
            }
            default: {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onUserDataChanged(@NonNull final UserData userData) {
    }

    private void updateUnreadIndicator(int count) {
        if (count > 0 && navItemIndex == NAV_DISCUSS) {
            DiscussController.getInstance().resetUnreadCount();
            count = 0;
        }
        String indicatorText = "";
        if (count > 0) {
            indicatorText = count > 99 ? "99+" : String.valueOf(count);
        }
        bottomNavigation.setNotification(indicatorText, 1);
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

        Runnable pendingRunnable = new Runnable() {
            @Override
            public void run() {
                Fragment fragment = createHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        handler.post(pendingRunnable);

        toggleFab();

        invalidateOptionsMenu();
    }

    @Nullable
    private HomeFragment getHomeFragment() {
        return (HomeFragment) getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
    }

    private HomeFragment createHomeFragment() {
        switch (navItemIndex) {
            case NAV_STORE:
                return new StoreFragment_();
            case NAV_UPLOADS:
                return new UserUploadsFragment_();
            case NAV_DISCUSS:
                return new DiscussFragment_();
            case NAV_PROFILE:
                return new ProfileFragment_();
            default:
                throw new IllegalStateException("Invalid navigation item index");
        }
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
        switch (id) {
            case R.id.menu_search:
                SearchActivity_.intent(this).start();
                return true;
            case R.id.nav_store:
                navItemIndex = NAV_STORE;
                CURRENT_TAG = TAG_STORE;
                break;
            case R.id.nav_uploads:
                navItemIndex = NAV_UPLOADS;
                CURRENT_TAG = TAG_UPLOADS;
                break;
            case R.id.nav_discuss:
                navItemIndex = NAV_DISCUSS;
                CURRENT_TAG = TAG_DISCUSS;
                break;
            case R.id.nav_settings:
                SettingsActivity_.intent(HomeActivity.this).start();
                return true;
            case R.id.nav_info:
                Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                throw new IllegalStateException("Invalid menu id");
        }
        loadHomeFragment();

        return false;
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
        if (navItemIndex == NAV_STORE) {
            getMenuInflater().inflate(R.menu.store_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.home_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onMenuSelected(item.getItemId());
    }

    private void toggleFab() {
        if (navItemIndex == NAV_STORE || navItemIndex == NAV_UPLOADS) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    private void checkForCrashes() {
        CrashManager.register(this);
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
            Intent intent = new Intent(this, DownloadActivity.class);
            intent.putExtra(DownloadActivity.STORE_APP_ID, item.getAppId());
            intent.putExtra(DownloadActivity.STORE_APP_LABEL, LocaleHelper.getLocalizedLabel(item));
            startActivity(intent);
        }
    }

    @Override
    public void onUnreadCount(int count) {
        updateUnreadIndicator(count);
    }

    @Override
    public void onShowIntro() {
    }

    @Override
    public void onUserNotReady() {
    }

    @Override
    public void onUserReady() {
    }

    private void checkMigration() {
        if (wasRegistered() && getLastRunBuildNumber() == 0) {
            MigrateActivity_.intent(this).start();
        }
    }

}
