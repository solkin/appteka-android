package com.tomclaw.appsend;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.tomclaw.appsend.main.controller.CountController;
import com.tomclaw.appsend.main.controller.UploadController;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.main.meta.MetaActivity_;
import com.tomclaw.appsend.util.FileHelper;
import com.tomclaw.appsend.util.IntentHelper;
import com.tomclaw.appsend.util.StringUtil;
import com.tomclaw.appsend.util.ThemeHelper;

import static com.tomclaw.appsend.util.IntentHelper.shareUrl;

/**
 * Created by ivsolkin on 02.01.17.
 */
public class UploadActivity extends AppCompatActivity implements UploadController.UploadCallback {

    private static final long DEBOUNCE_DELAY = 100;
    public static final String UPLOAD_ITEM = "app_info";
    private static final int REQUEST_UPDATE_META = 4;
    private static final String META_ACTIVITY_SHOWN = "meta_activity_shown";

    private CommonItem item;
    private ImageView appIcon;
    private TextView appLabel;
    private TextView appPackage;
    private TextView appVersion;
    private TextView appSize;
    private ProgressBar progress;
    private TextView percent;

    private ViewSwitcher viewSwitcher;

    private static PackageIconGlideLoader loader;
    private String url;

    private transient long progressUpdateTime = 0;

    private boolean isMetaActivityShown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            isMetaActivityShown = savedInstanceState.getBoolean(META_ACTIVITY_SHOWN);
        }

        setContentView(R.layout.upload_activity);
        ThemeHelper.updateStatusBar(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        boolean isCreateInstance = savedInstanceState == null;
        if (isCreateInstance) {
            item = getIntent().getParcelableExtra(UPLOAD_ITEM);
        } else {
            item = savedInstanceState.getParcelable(UPLOAD_ITEM);
        }

        appIcon = (ImageView) findViewById(R.id.app_icon);
        appLabel = (TextView) findViewById(R.id.app_label);
        appPackage = (TextView) findViewById(R.id.app_package);
        appVersion = (TextView) findViewById(R.id.app_version);
        appSize = (TextView) findViewById(R.id.app_size);
        progress = (ProgressBar) findViewById(R.id.progress);
        percent = (TextView) findViewById(R.id.percent);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.view_switcher);
        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.button_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareUrl(UploadActivity.this, formatText());
            }
        });
        findViewById(R.id.button_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringUtil.copyStringToClipboard(UploadActivity.this, formatText());
                Toast.makeText(UploadActivity.this, R.string.url_copied, Toast.LENGTH_SHORT).show();
            }
        });

        if (loader == null) {
            loader = new PackageIconGlideLoader(getPackageManager());
        }

        PackageInfo packageInfo = item.getPackageInfo();

        if (packageInfo != null) {
            Glide.with(this)
                    .using(loader)
                    .load(packageInfo)
                    .into(appIcon);
        }

        appLabel.setText(item.getLabel());
        appPackage.setText(item.getPackageName());
        String size = FileHelper.formatBytes(getResources(), item.getSize());
        appSize.setText(getString(R.string.upload_size, size));
        appVersion.setText(item.getVersion());

        if (isCreateInstance) {
            UploadController.getInstance().upload(item);
        }
    }

    private String formatText() {
        return IntentHelper.formatText(getResources(), url, item.getLabel(), item.getSize());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_UPDATE_META) {
            viewSwitcher.setDisplayedChild(1);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        UploadController.getInstance().onAttach(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        UploadController.getInstance().onDetach(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(UPLOAD_ITEM, item);
        outState.putBoolean(META_ACTIVITY_SHOWN, isMetaActivityShown);
    }

    @Override
    public void onProgress(int percent) {
        if (System.currentTimeMillis() - progressUpdateTime >= DEBOUNCE_DELAY) {
            progressUpdateTime = System.currentTimeMillis();
            this.percent.setText(getString(R.string.percent, percent));
            progress.setProgress(percent);
        }
    }

    @Override
    public void onUploaded() {
        percent.setText(R.string.obtaining_link_message);
        progress.setIndeterminate(true);
    }

    @Override
    public void onCompleted(String url) {
        this.url = url;
        if (isMetaActivityShown) {
            viewSwitcher.setDisplayedChild(1);
        } else {
            MetaActivity_.intent(this)
                    .commonItem(item)
                    .startForResult(REQUEST_UPDATE_META);
            isMetaActivityShown = true;
        }
        CountController.getInstance().load(this);
    }

    @Override
    public void onError() {
        Toast.makeText(this, R.string.uploading_error, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (UploadController.getInstance().isCompleted()) {
            finish();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.cancel_upload_title))
                    .setMessage(getString(R.string.cancel_upload_text))
                    .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UploadController.getInstance().cancel();
                            finish();
                        }
                    })
                    .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
    }
}
