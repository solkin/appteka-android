package com.tomclaw.appsend;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.PermisoActivity;
import com.jaeger.library.StatusBarUtil;
import com.kobakei.ratethisapp.RateThisApp;
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.main.adapter.AppInfoAdapter;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.task.ExportApkTask;
import com.tomclaw.appsend.main.task.UpdateAppListTask;
import com.tomclaw.appsend.main.task.UploadApkTask;
import com.tomclaw.appsend.util.PreferenceHelper;
import com.tomclaw.appsend.util.ThemeHelper;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.metrics.MetricsManager;

import java.util.List;

public class MainActivity extends PermisoActivity implements BillingProcessor.IBillingHandler {

    private static final int REQUEST_UPDATE_SETTINGS = 6;
    private RecyclerView listView;
    private AppInfoAdapter adapter;
    private AppInfoAdapter.AppItemClickListener listener;
    private SearchView.OnQueryTextListener onQueryTextListener;
    private boolean isRefreshOnResume = false;
    private BillingProcessor bp;
    private boolean isDarkTheme;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDarkTheme = ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        String licenseKey = getString(R.string.license_key);
        bp = new BillingProcessor(this, licenseKey, this);

        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.ic_logo_ab);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        listView = (RecyclerView) findViewById(R.id.apps_list_view);
        listView.setLayoutManager(layoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        listView.setItemAnimator(itemAnimator);

        listener = new AppInfoAdapter.AppItemClickListener() {
            @Override
            public void onItemClicked(final AppInfo appInfo) {
                boolean donateItem = (appInfo.getFlags() & AppInfo.FLAG_DONATE_ITEM) == AppInfo.FLAG_DONATE_ITEM;
                if (donateItem) {
                    showDonateDialog();
                } else {
                    checkPermissionsForExtract(appInfo);
                }
            }
        };

        adapter = new AppInfoAdapter(this);
        adapter.setListener(listener);
        listView.setAdapter(adapter);

        onQueryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        };

        FloatingActionButton actionButton = (FloatingActionButton) findViewById(R.id.fab);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsForInstall();
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) actionButton.getLayoutParams();
            p.setMargins(0, 0, 0, 0); // get rid of margins since shadow area is now the margin
            actionButton.setLayoutParams(p);
        }

        refreshAppList();

        int color = getResources().getColor(R.color.action_bar_color);
        StatusBarUtil.setColor(this, color);

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

        checkForCrashes();
        MetricsManager.register(this, getApplication());
    }

    private void showActionDialog(final AppInfo appInfo) {
        ListAdapter menuAdapter = new MenuAdapter(MainActivity.this, R.array.app_actions_titles, R.array.app_actions_icons);
        new AlertDialog.Builder(MainActivity.this)
                .setAdapter(menuAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                if (appInfo.getLaunchIntent() == null) {
                                    Snackbar.make(listView, R.string.non_launchable_package, Snackbar.LENGTH_LONG).show();
                                } else {
                                    startActivity(appInfo.getLaunchIntent());
                                }
                                break;
                            }
                            case 1: {
                                TaskExecutor.getInstance().execute(new ExportApkTask(MainActivity.this, appInfo, ExportApkTask.ACTION_SHARE));
                                break;
                            }
                            case 2: {
                                TaskExecutor.getInstance().execute(new ExportApkTask(MainActivity.this, appInfo, ExportApkTask.ACTION_EXTRACT));
                                break;
                            }
                            case 3: {
                                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                                intent.putExtra(UploadActivity.APP_INFO, appInfo);
                                startActivity(intent);
                                break;
                            }
                            case 4: {
                                TaskExecutor.getInstance().execute(new ExportApkTask(MainActivity.this, appInfo, ExportApkTask.ACTION_BLUETOOTH));
                                break;
                            }
                            case 5: {
                                final String appPackageName = appInfo.getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                                break;
                            }
                            case 6: {
                                isRefreshOnResume = true;
                                final Intent intent = new Intent()
                                        .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .addCategory(Intent.CATEGORY_DEFAULT)
                                        .setData(Uri.parse("package:" + appInfo.getPackageName()))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                break;
                            }
                            case 7: {
                                isRefreshOnResume = true;
                                Uri packageUri = Uri.parse("package:" + appInfo.getPackageName());
                                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                                startActivity(uninstallIntent);
                                break;
                            }
                        }
                    }
                }).show();
    }

    private void showDonateDialog() {
        startActivity(new Intent(this, DonateActivity.class));
    }

    private void checkPermissionsForExtract(final AppInfo appInfo) {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    // Permission granted!
                    showActionDialog(appInfo);
                } else {
                    // Permission denied.
                    Snackbar.make(listView, R.string.permission_denied_message, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                String title = getString(R.string.app_name);
                String message = getString(R.string.write_permission_extract);
                Permiso.getInstance().showRationaleInDialog(title, message, null, callback);
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void checkPermissionsForInstall() {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    // Permission granted!
                    Intent intent = new Intent(MainActivity.this, InstallActivity.class);
                    startActivity(intent);
                } else {
                    // Permission denied.
                    Snackbar.make(listView, R.string.permission_denied_message, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                String title = getString(R.string.app_name);
                String message = getString(R.string.write_permission_install);
                Permiso.getInstance().showRationaleInDialog(title, message, null, callback);
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void refreshAppList() {
        TaskExecutor.getInstance().execute(new UpdateAppListTask(this));
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isRefreshOnResume) {
            refreshAppList();
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
        getMenuInflater().inflate(R.menu.main_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(menu.findItem(R.id.menu_search).getTitle());
        // Configure the search info and add any event listeners
        searchView.setOnQueryTextListener(onQueryTextListener);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                break;
            }
            case R.id.refresh: {
                refreshAppList();
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
                refreshAppList();
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

    public void setAppInfoList(List<AppInfo> appInfoList) {
        if (bp.loadOwnedPurchasesFromGoogle() &&
                bp.isPurchased(getString(R.string.chocolate_id))) {
            for (AppInfo appInfo : appInfoList) {
                boolean donateItem = (appInfo.getFlags() & AppInfo.FLAG_DONATE_ITEM) == AppInfo.FLAG_DONATE_ITEM;
                if (donateItem) {
                    appInfoList.remove(appInfo);
                    break;
                }
            }
        }
        adapter.setAppInfoList(appInfoList);
        adapter.notifyDataSetChanged();
    }

    private void checkForCrashes() {
        CrashManager.register(this);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
    }

    @Override
    public void onBillingInitialized() {
    }
}
