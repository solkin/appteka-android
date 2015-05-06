package com.tomclaw.appsend;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_UPDATE_SETTINGS = 6;
    private RecyclerView listView;
    private AppInfoAdapter adapter;
    private AppInfoAdapter.AppItemClickListener listener;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setItems(R.array.app_actions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: {
                                        Uri uri = Uri.fromFile(new File(appInfo.getPath()));
                                        Intent sendIntent = new Intent();
                                        sendIntent.setAction(Intent.ACTION_SEND);
                                        sendIntent.putExtra(Intent.EXTRA_TEXT, appInfo.getLabel());
                                        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                        sendIntent.setType("application/zip");
                                        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
                                        break;
                                    }
                                    case 1: {
                                        TaskExecutor.getInstance().execute(new ExportApkTask(MainActivity.this, appInfo));
                                        break;
                                    }
                                    case 2: {
                                        TaskExecutor.getInstance().execute(new UploadApkTask(MainActivity.this, appInfo));
                                        break;
                                    }
                                    case 3: {
                                        Uri uri = Uri.fromFile(new File(appInfo.getPath()));
                                        Intent sendIntent = new Intent();
                                        sendIntent.setAction(Intent.ACTION_SEND);
                                        sendIntent.putExtra(Intent.EXTRA_TEXT, appInfo.getLabel());
                                        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                        sendIntent.setType("application/zip");
                                        sendIntent.setPackage("com.android.bluetooth");
                                        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
                                        break;
                                    }
                                    case 4: {
                                        final String appPackageName = appInfo.getPackageName();
                                        try {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                        }
                                        break;
                                    }
                                    case 5: {
                                        Uri packageUri = Uri.parse("package:" + appInfo.getPackageName());
                                        Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                                        startActivity(uninstallIntent);
                                        break;
                                    }
                                }
                            }
                        }).show();
            }
        };

        refreshAppList();

        Utils.setupTint(this);
    }

    private void refreshAppList() {
        TaskExecutor.getInstance().execute(new UpdateAppListTask(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_UPDATE_SETTINGS);
    }

    private void showInfo() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void setAppInfoList(List<AppInfo> appInfoList) {
        adapter = new AppInfoAdapter(this, appInfoList);
        adapter.setListener(listener);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_UPDATE_SETTINGS) {
            if (resultCode == SettingsActivity.RESULT_UPDATE) {
                refreshAppList();
            }
        }
    }
}
