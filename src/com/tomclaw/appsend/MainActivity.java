package com.tomclaw.appsend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends Activity {

    private static final int REQUEST_UPDATE_SETTINGS = 6;
    private ListView listView;
    private AppInfoAdapter adapter;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        listView = (ListView) findViewById(R.id.apps_list_view);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final AppInfo appInfo = adapter.getItem(position);
                TaskExecutor.getInstance().execute(new ExportApkTask(view.getContext(), appInfo));
            }
        });

        refreshAppList();
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
        listView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_UPDATE_SETTINGS) {
            if(resultCode == SettingsActivity.RESULT_UPDATE) {
                refreshAppList();
            }
        }
    }
}
