package com.tomclaw.appsend;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.view.PlayView;
import com.tomclaw.appsend.util.ThemeHelper;

/**
 * Created by ivsolkin on 14.01.17.
 */
public class DownloadActivity extends AppCompatActivity {

    public static final String STORE_ITEM = "store_info";

    private StoreItem item;

    private ImageView appIcon;
    private TextView appLabel;
    private TextView appPackage;
    private TextView appVersion;
    private PlayView appDownloads;
    private PlayView appSize;
    private PlayView minAndroid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.download_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        boolean isCreateInstance = savedInstanceState == null;
        if (isCreateInstance) {
            item = getIntent().getParcelableExtra(STORE_ITEM);
        } else {
            item = savedInstanceState.getParcelable(STORE_ITEM);
        }

        appIcon = (ImageView) findViewById(R.id.app_icon);
        appLabel = (TextView) findViewById(R.id.app_label);
        appPackage = (TextView) findViewById(R.id.app_package);
        appDownloads = (PlayView) findViewById(R.id.app_downloads);
        appSize = (PlayView) findViewById(R.id.app_size);
        minAndroid = (PlayView) findViewById(R.id.min_android);

        bindData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STORE_ITEM, item);
    }

    private void bindData() {
        Glide.with(this)
                .load(item.getIcon())
                .into(appIcon);
        String sizeText;
        int sizeFactor;
        long bytes = item.getSize();
        if (bytes < 1024 * 1024) {
            sizeText = String.format("%.1f", bytes / 1024.0f);
            sizeFactor = R.string.kilobytes;
        } else if (bytes < 10 * 1024 * 1024) {
            sizeText = String.format("%.1f", bytes / 1024.0f / 1024.0f);
            sizeFactor = R.string.megabytes;
        } else {
            sizeText = String.format("%d", bytes / 1024 / 1024);
            sizeFactor = R.string.megabytes;
        }
        appLabel.setText(item.getLabel());
        appPackage.setText(item.getPackageName());
        appDownloads.setCount(String.valueOf(item.getDownloads()));
        appSize.setCount(sizeText);
        appSize.setDescription(getString(sizeFactor));
        minAndroid.setCount(String.valueOf(item.getSdkVersion()));
    }
}
