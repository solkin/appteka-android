package com.tomclaw.appsend.main.upload;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.GlideApp;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.main.meta.MetaActivity;
import com.tomclaw.appsend.util.FileHelper;
import com.tomclaw.appsend.util.IntentHelper;
import com.tomclaw.appsend.util.StringUtil;
import com.tomclaw.appsend.util.ThemeHelper;

import static com.tomclaw.appsend.util.Analytics.trackEvent;
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

    private String appId;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
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

        appIcon = findViewById(R.id.app_icon);
        appLabel = findViewById(R.id.app_label);
        appPackage = findViewById(R.id.app_package);
        appVersion = findViewById(R.id.app_version);
        appSize = findViewById(R.id.app_size);
        progress = findViewById(R.id.progress);
        percent = findViewById(R.id.percent);
        viewSwitcher = findViewById(R.id.view_switcher);
        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.button_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UploadActivity.this, DownloadActivity.class);
                intent.putExtra(DownloadActivity.STORE_APP_ID, appId);
                intent.putExtra(DownloadActivity.STORE_APP_LABEL, item.getLabel());
                startActivity(intent);
                trackEvent("click-uploaded-share");
            }
        });
        findViewById(R.id.button_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareUrl(UploadActivity.this, formatText());
                trackEvent("click-uploaded-open");
            }
        });
        findViewById(R.id.button_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringUtil.copyStringToClipboard(UploadActivity.this, formatText());
                Toast.makeText(UploadActivity.this, R.string.url_copied, Toast.LENGTH_SHORT).show();
                trackEvent("click-uploaded-copy");
            }
        });

        PackageInfo packageInfo = item.getPackageInfo();

        if (packageInfo != null) {
            GlideApp.with(this)
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
        super.onActivityResult(requestCode, resultCode, data);
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
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
    public void onCompleted(String appId, String url) {
        this.appId = appId;
        this.url = url;
        if (isMetaActivityShown) {
            viewSwitcher.setDisplayedChild(1);
        } else {
            Intent intent = new Intent(this, MetaActivity.class)
                    .putExtra(MetaActivity.APP_ID_EXTRA, appId)
                    .putExtra(MetaActivity.COMMON_ITEM_EXTRA, item);
            startActivityForResult(intent, REQUEST_UPDATE_META);
            isMetaActivityShown = true;
        }
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
