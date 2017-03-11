package com.tomclaw.appsend;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ViewFlipper;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.greysonparrelli.permiso.PermisoActivity;
import com.kobakei.ratethisapp.RateThisApp;
import com.tomclaw.appsend.main.controller.CountController;
import com.tomclaw.appsend.main.controller.StoreController;
import com.tomclaw.appsend.main.view.AppsView;
import com.tomclaw.appsend.main.view.InstallView;
import com.tomclaw.appsend.main.view.MainView;
import com.tomclaw.appsend.main.view.StoreView;
import com.tomclaw.appsend.util.ColorHelper;
import com.tomclaw.appsend.util.PreferenceHelper;
import com.tomclaw.appsend.util.ThemeHelper;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.metrics.MetricsManager;

public class MainActivity extends PermisoActivity implements MainView.ActivityCallback, CountController.CountCallback {

    private static final int REQUEST_UPDATE_SETTINGS = 6;
    private ViewFlipper mainViewsContainer;
    private MainView mainView;
    private SearchView.OnQueryTextListener onQueryTextListener;
    private boolean isRefreshOnResume = false;
    private boolean isDarkTheme;
    private CountController countController = CountController.getInstance();
    private StoreController storeController = StoreController.getInstance();
    private AHBottomNavigation bottomNavigation;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDarkTheme = ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        boolean isCreateInstance = savedInstanceState == null;

        setContentView(R.layout.main);
        ThemeHelper.updateStatusBar(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.ic_logo_ab);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_apps, R.drawable.ic_apps, R.color.primary_color);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_install, R.drawable.ic_install, R.color.primary_color);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_store, R.drawable.ic_store, R.color.primary_color);

        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);

        bottomNavigation.setDefaultBackgroundColor(ColorHelper.getAttributedColor(this, R.attr.bottom_bar_background));
        bottomNavigation.setAccentColor(getResources().getColor(R.color.accent_color));
        bottomNavigation.setInactiveColor(getResources().getColor(R.color.grey_dark));
        bottomNavigation.setForceTint(true);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                selectTab(position);
                return true;
            }
        });

        mainViewsContainer = (ViewFlipper) findViewById(R.id.main_views);
        AppsView appsView = new AppsView(this);
        mainViewsContainer.addView(appsView);
        InstallView installView = new InstallView(this);
        mainViewsContainer.addView(installView);
        StoreView storeView = new StoreView(this);
        mainViewsContainer.addView(storeView);
        mainViewsContainer.setDisplayedChild(0);

        onQueryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mainView.isFilterable()) {
                    mainView.filter(newText);
                }
                return false;
            }
        };

        bottomNavigation.post(new Runnable() {
            @Override
            public void run() {
                selectTab(bottomNavigation.getCurrentItem());
            }
        });

        if (savedInstanceState == null) {
            // Custom criteria: 7 days and 10 launches
            RateThisApp.Config config = new RateThisApp.Config(7, 10);
            // Custom title ,message and buttons names
            config.setTitle(R.string.rate_title);
            config.setMessage(R.string.rate_message);
            config.setYesButtonText(R.string.yes_rate);
            config.setNoButtonText(R.string.no_thanks);
            config.setCancelButtonText(R.string.rate_cancel);
            RateThisApp.init(config);

            // Monitor launch times and interval from installation
            RateThisApp.onStart(this);
            // If the criteria is satisfied, "Rate this app" dialog will be shown
            RateThisApp.showRateDialogIfNeeded(this);
        }

        checkForCrashes();
        MetricsManager.register(this, getApplication());

        if (isCreateInstance) {
            loadStoreCount();
        }
    }

    private void selectTab(int position) {
        switch (position) {
            case 0:
                showApps();
                break;
            case 1:
                showInstall();
                break;
            case 2:
                showStore();
                break;
        }
    }

    private void showApps() {
        switchMainView(0);
    }

    private void showInstall() {
        switchMainView(1);
    }

    private void showStore() {
        if (countController.resetCount()) {
            storeController.reload(this);
        }
        switchMainView(2);
    }

    private void switchMainView(int index) {
        mainViewsContainer.setDisplayedChild(index);
        mainView = (MainView) mainViewsContainer.getChildAt(index);
        mainView.activate(this);

        invalidateOptionsMenu();
    }

    private void updateList() {
        mainView.refresh();
    }

    private void loadStoreCount() {
        countController.load(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        operateMainViews(new MainViewOperation() {
            @Override
            public void invoke(MainView mainView) {
                mainView.start();
            }
        });
        countController.onAttach(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        operateMainViews(new MainViewOperation() {
            @Override
            public void invoke(MainView mainView) {
                mainView.stop();
            }
        });
        countController.onDetach(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        operateMainViews(new MainViewOperation() {
            @Override
            public void invoke(MainView mainView) {
                mainView.destroy();
            }
        });
    }

    private void operateMainViews(MainViewOperation operation) {
        for (int c = 0; c < mainViewsContainer.getChildCount(); c++) {
            MainView mainView = (MainView) mainViewsContainer.getChildAt(c);
            operation.invoke(mainView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRefreshOnResume) {
            updateList();
            isRefreshOnResume = false;
        }
        if (isDarkTheme != PreferenceHelper.isDarkTheme(this)) {
            Intent intent = getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        @MenuRes int menuRes;
        if (mainView == null || !mainView.isFilterable()) {
            menuRes = R.menu.main_no_search_menu;
        } else {
            menuRes = R.menu.main_menu;
        }
        getMenuInflater().inflate(menuRes, menu);
        MenuItem searchMenu = menu.findItem(R.id.menu_search);
        if (searchMenu != null) {
            SearchView searchView = (SearchView) searchMenu.getActionView();
            searchView.setQueryHint(menu.findItem(R.id.menu_search).getTitle());
            // Configure the search info and add any event listeners
            searchView.setOnQueryTextListener(onQueryTextListener);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                break;
            }
            case R.id.refresh: {
                updateList();
                loadStoreCount();
                break;
            }
            case R.id.settings: {
                showSettings();
                break;
            }
            case R.id.info: {
                showInfo();
                break;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_UPDATE_SETTINGS) {
            if (resultCode == SettingsActivity.RESULT_UPDATE) {
                updateList();
            }
        }
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_UPDATE_SETTINGS);
    }

    private void showInfo() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void checkForCrashes() {
        CrashManager.register(this);
    }

    @Override
    public void setRefreshOnResume() {
        isRefreshOnResume = true;
    }

    @Override
    public void onProgress() {
    }

    @Override
    public void onLoaded(int count) {
        String notification = "";
        if (count > 0) {
            notification = String.valueOf(count);
        }
        bottomNavigation.setNotification(notification, 2);
    }

    @Override
    public void onError() {
    }

    private interface MainViewOperation {

        void invoke(MainView mainView);
    }
}
