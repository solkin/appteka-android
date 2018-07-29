package com.tomclaw.appsend.main.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.main.about.AboutActivity_;
import com.tomclaw.appsend.main.controller.DiscussController;
import com.tomclaw.appsend.main.controller.UpdateController;
import com.tomclaw.appsend.main.discuss.DiscussFragment_;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.local.HomeDistroFragment_;
import com.tomclaw.appsend.main.local.HomeInstalledFragment_;
import com.tomclaw.appsend.main.local.SelectLocalAppActivity;
import com.tomclaw.appsend.main.local.SelectLocalAppActivity_;
import com.tomclaw.appsend.main.profile.ProfileActivity_;
import com.tomclaw.appsend.main.settings.SettingsActivity_;
import com.tomclaw.appsend.main.store.StoreFragment_;
import com.tomclaw.appsend.main.store.UserUploadsFragment_;
import com.tomclaw.appsend.main.upload.UploadActivity;
import com.tomclaw.appsend.main.view.MemberImageView;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.net.UserData;
import com.tomclaw.appsend.net.UserDataListener;
import com.tomclaw.appsend.util.LocaleHelper;
import com.tomclaw.appsend.util.PreferenceHelper;
import com.tomclaw.appsend.util.ThemeHelper;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.metrics.MetricsManager;

import static com.tomclaw.appsend.util.MemberImageHelper.memberImageHelper;

public class HomeActivity extends AppCompatActivity implements UserDataListener,
        UpdateController.UpdateCallback,
        DiscussController.DiscussCallback {

    private static final int NAV_STORE = 0;
    private static final int NAV_UPLOADS = 1;
    private static final int NAV_DISCUSS = 2;
    private static final int NAV_INSTALLED = 3;
    private static final int NAV_DISTRO = 4;

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

    public static int navItemIndex = 0;

    private static final String TAG_STORE = "store";
    private static final String TAG_UPLOADS = "uploads";
    private static final String TAG_DISCUSS = "discuss";
    private static final String TAG_INSTALLED = "installed";
    private static final String TAG_DISTRO = "distro";
    public static String CURRENT_TAG = TAG_STORE;

    private String[] activityTitles;

    private final static boolean shouldLoadHomeFragOnBackPress = true;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isCreateInstance = savedInstanceState == null;

        isDarkTheme = ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
        ThemeHelper.updateStatusBar(this);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        activityTitles = new String[]{
                getString(R.string.nav_store),
                getString(R.string.nav_uploads),
                getString(R.string.nav_discuss),
                getString(R.string.nav_installed),
                getString(R.string.nav_distro)
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
                SelectLocalAppActivity_.intent(HomeActivity.this).startForResult(REQUEST_UPLOAD);
            }
        });

        setUpNavigationView();

        setToolbarTitle();

        if (savedInstanceState == null) {
            navItemIndex = NAV_STORE;
            CURRENT_TAG = TAG_STORE;
            loadHomeFragment();
        }

        if (isCreateInstance) {
            checkForUpdates();
        }

        checkForCrashes();
        MetricsManager.register(getApplication());
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
                CommonItem item = data.getParcelableExtra(SelectLocalAppActivity.SELECTED_ITEM);
                Intent intent = new Intent(this, UploadActivity.class);
                intent.putExtra(UploadActivity.UPLOAD_ITEM, item);
                startActivity(intent);
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
        boolean isThreadOwner = userId == 1;

        txtName.setText(memberImageHelper().getName(userId, isThreadOwner));
        txtWebsite.setText(String.valueOf(userId));

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

        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();

            toggleFab();
            return;
        }

        Runnable pendingRunnable = new Runnable() {
            @Override
            public void run() {
                Fragment fragment = getHomeFragment();
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

    private Fragment getHomeFragment() {
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
            default:
                throw new IllegalStateException("Invalid navigation item index");
        }
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
                        AboutActivity_.intent(HomeActivity.this).start();
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

        if (id == R.id.profile) {
            Toast.makeText(getApplicationContext(), "User Profile", Toast.LENGTH_LONG).show();
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
}
