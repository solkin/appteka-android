package com.tomclaw.appsend;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.main.adapter.AppInfoAdapter;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.task.ExportApkTask;
import com.tomclaw.appsend.main.task.ScanApkOnStorageTask;
import com.tomclaw.appsend.main.task.UploadApkTask;
import com.tomclaw.appsend.util.ThemeHelper;

import java.io.File;
import java.util.List;

/**
 * Created by ivsolkin on 01.09.16.
 */

public class InstallActivity extends AppCompatActivity {

    private RecyclerView listView;
    private TextView installHelp;
    private AppInfoAdapter adapter;
    private AppInfoAdapter.AppItemClickListener listener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.install);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        installHelp = (TextView) findViewById(R.id.install_help);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        listView = (RecyclerView) findViewById(R.id.apps_list_view);
        listView.setLayoutManager(layoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        listView.setItemAnimator(itemAnimator);

        listener = new AppInfoAdapter.AppItemClickListener() {
            @Override
            public void onItemClicked(final AppInfo appInfo) {
                showActionDialog(appInfo);
            }
        };

        adapter = new AppInfoAdapter(this);
        adapter.setListener(listener);
        listView.setAdapter(adapter);

        refreshAppList();

        int color = getResources().getColor(R.color.action_bar_color);
        StatusBarUtil.setColor(this, color);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.install_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.refresh: {
                refreshAppList();
                break;
            }
        }
        return true;
    }

    private void installApp(AppInfo appInfo) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(appInfo.getPath())), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void refreshAppList() {
        TaskExecutor.getInstance().execute(new ScanApkOnStorageTask(this));
    }

    public void setAppInfoList(List<AppInfo> appInfoList) {
        adapter.setAppInfoList(appInfoList);
        adapter.notifyDataSetChanged();
        installHelp.setText(appInfoList.isEmpty() ? R.string.install_screen_no_apk_found: R.string.install_screen_apk_found);
    }

    private void showActionDialog(final AppInfo appInfo) {
        ListAdapter menuAdapter = new MenuAdapter(InstallActivity.this, R.array.apk_actions_titles, R.array.apk_actions_icons);
        new AlertDialog.Builder(InstallActivity.this)
                .setAdapter(menuAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                installApp(appInfo);
                                break;
                            }
                            case 1: {
                                ExportApkTask.shareApk(InstallActivity.this, new File(appInfo.getPath()));
                                break;
                            }
                            case 2: {
                                TaskExecutor.getInstance().execute(new UploadApkTask(InstallActivity.this, appInfo));
                                break;
                            }
                            case 3: {
                                ExportApkTask.bluetoothApk(InstallActivity.this, appInfo);
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
                                File file = new File(appInfo.getPath());
                                file.delete();
                                refreshAppList();
                                break;
                            }
                        }
                    }
                }).show();
    }
}
