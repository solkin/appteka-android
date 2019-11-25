package com.tomclaw.appsend.main.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.greysonparrelli.permiso.PermisoActivity;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.main.about.AboutActivity;
import com.tomclaw.appsend.main.controller.DiscussController;
import com.tomclaw.appsend.main.controller.UpdateController;
import com.tomclaw.appsend.main.discuss.DiscussFragment_;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.local.DialogData;
import com.tomclaw.appsend.main.local.HomeDistroFragment_;
import com.tomclaw.appsend.main.local.HomeInstalledFragment_;
import com.tomclaw.appsend.main.local.SelectLocalAppActivity;
import com.tomclaw.appsend.main.local.SelectLocalAppActivity_;
import com.tomclaw.appsend.main.migrate.MigrateActivity_;
import com.tomclaw.appsend.main.profile.ProfileActivity_;
import com.tomclaw.appsend.main.profile.ProfileFragment_;
import com.tomclaw.appsend.main.settings.SettingsActivity_;
import com.tomclaw.appsend.main.store.StoreFragment_;
import com.tomclaw.appsend.main.store.UserUploadsFragment_;
import com.tomclaw.appsend.main.store.search.SearchActivity_;
import com.tomclaw.appsend.main.upload.UploadActivity;
import com.tomclaw.appsend.main.view.MemberImageView;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.net.UserData;
import com.tomclaw.appsend.net.UserDataListener;
import com.tomclaw.appsend.util.KeyboardHelper;
import com.tomclaw.appsend.util.LocaleHelper;
import com.tomclaw.appsend.util.PreferenceHelper;
import com.tomclaw.appsend.util.ThemeHelper;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.metrics.MetricsManager;

import static com.tomclaw.appsend.AppSend.getLastRunBuildNumber;
import static com.tomclaw.appsend.AppSend.wasRegistered;
import static com.tomclaw.appsend.util.MemberImageHelper.memberImageHelper;

public class HomeActivity extends PermisoActivity implements UserDataListener,
        UpdateController.UpdateCallback,
        DiscussController.DiscussCallback {

    public static final String ACTION_INSTALLED = "com.tomclaw.appsend.apps";
    public static final String ACTION_DISTRO = "com.tomclaw.appsend.install";
    public static final String ACTION_STORE = "com.tomclaw.appsend.cloud";
    public static final String ACTION_DISCUSS = "com.tomclaw.appsend.discuss";

    private static final int NAV_STORE = 0;
    private static final int NAV_UPLOADS = 1;
    private static final int NAV_DISCUSS = 2;
    private static final int NAV_INSTALLED = 3;
    private static final int NAV_DISTRO = 4;
    private static final int NAV_PROFILE = 5;

    private static final int REQUEST_UPLOAD = 4;

    private View updateBlock;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private MemberImageView imgProfile;
    private TextView txtName, txtWebsite;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private boolean isDarkTheme;
    private View actionView;
    private TextView unreadIndicator;
    private AHBottomNavigation bottomNavigation;

    public static int navItemIndex = 0;

    private static final String TAG_STORE = "store";
    private static final String TAG_UPLOADS = "uploads";
    private static final String TAG_DISCUSS = "discuss";
    private static final String TAG_INSTALLED = "installed";
    private static final String TAG_DISTRO = "distro";
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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ThemeHelper.updateStatusBar(this);

        String intentAction = getIntent().getAction();

        handler = new Handler();

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        fab = findViewById(R.id.fab);

        actionView = getLayoutInflater().inflate(R.layout.menu_indicator, navigationView, false);
        unreadIndicator = actionView.findViewById(R.id.indicator);

        View navHeader = navigationView.getHeaderView(0);
        txtName = navHeader.findViewById(R.id.name);
        txtWebsite = navHeader.findViewById(R.id.website);
        imgProfile = navHeader.findViewById(R.id.img_profile);

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

        navHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity_.intent(HomeActivity.this).start();
                drawer.closeDrawers();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogData dialogData = new DialogData(getString(R.string.upload_app_title), getString(R.string.upload_app_message));
                SelectLocalAppActivity_.intent(HomeActivity.this).dialogData(dialogData).startForResult(REQUEST_UPLOAD);
            }
        });

        setUpNavigationView();

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
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                showNavHeader(userData);
            }
        });
    }

    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, website, notifications action view (dot)
     */
    void showNavHeader(UserData userData) {
        long userId = userData.getUserId();

        String name = userData.getName();
        if (TextUtils.isEmpty(name)) {
            boolean isThreadOwner = userId == 1;
            name = getString(memberImageHelper().getName(userId, isThreadOwner));
        }
        String description = userData.getEmail();
        if (TextUtils.isEmpty(description)) {
            description = String.valueOf(userId);
        }
        txtName.setText(name);
        txtWebsite.setText(description);

        imgProfile.setMemberId(userId);
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
        unreadIndicator.setText(indicatorText);
        bottomNavigation.setNotification(indicatorText, 1);
        MenuItem menuItem = navigationView.getMenu().getItem(NAV_DISCUSS);
        if (TextUtils.isEmpty(indicatorText)) {
            menuItem.setActionView(null);
        } else {
            menuItem.setActionView(actionView);
        }
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        selectNavMenu();

        setToolbarTitle();

        HomeFragment currentFragment = getHomeFragment();
        if (currentFragment != null) {
            drawer.closeDrawers();
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

        drawer.closeDrawers();

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
            case NAV_INSTALLED:
                return new HomeInstalledFragment_();
            case NAV_DISTRO:
                return new HomeDistroFragment_();
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
                case ACTION_INSTALLED:
                    navItemIndex = NAV_INSTALLED;
                    CURRENT_TAG = TAG_INSTALLED;
                    return true;
                case ACTION_DISTRO:
                    navItemIndex = NAV_DISTRO;
                    CURRENT_TAG = TAG_DISTRO;
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

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
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
                    case R.id.nav_installed:
                        navItemIndex = NAV_INSTALLED;
                        CURRENT_TAG = TAG_INSTALLED;
                        break;
                    case R.id.nav_distro:
                        navItemIndex = NAV_DISTRO;
                        CURRENT_TAG = TAG_DISTRO;
                        break;
                    case R.id.nav_settings:
                        SettingsActivity_.intent(HomeActivity.this).start();
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_info:
                        Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
                        startActivity(intent);
                        drawer.closeDrawers();
                        return true;
                    default:
                        throw new IllegalStateException("Invalid menu id");
                }

                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we don't want anything
                // to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we don't want anything
                // to happen so we leave this blank
                super.onDrawerOpened(drawerView);
                KeyboardHelper.hideKeyboard(HomeActivity.this);
            }
        };

        drawer.removeDrawerListener(actionBarDrawerToggle);
        drawer.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

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
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_search) {
            SearchActivity_.intent(this).start();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
