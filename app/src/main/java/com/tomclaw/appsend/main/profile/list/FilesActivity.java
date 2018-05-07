package com.tomclaw.appsend.main.profile.list;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.LocaleHelper;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tomclaw.appsend.util.PackageHelper.getInstalledVersionCode;

/**
 * Created by Igor on 22.10.2017.
 */
@EActivity(R.layout.user_files_activity)
public class FilesActivity extends AppCompatActivity implements FilesListener {

    @Bean
    StoreServiceHolder serviceHolder;

    @ViewById
    Toolbar toolbar;

    @ViewById
    ViewFlipper viewFlipper;

    @ViewById
    RecyclerView filesView;

    @ViewById
    TextView errorText;

    @ViewById
    Button retryButton;

    @InstanceState
    ArrayList<StoreItem> files;

    @InstanceState
    boolean isError;

    @InstanceState
    boolean isLoading;

    @InstanceState
    boolean isLoadedAll;

    @Extra
    Long userId;

    private FilesAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        ThemeHelper.updateStatusBar(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        adapter = new FilesAdapter(this);
        adapter.setHasStableIds(true);
        adapter.setListener(this);
        filesView.setLayoutManager(layoutManager);
        filesView.setAdapter(adapter);

        if (files == null) {
            showProgress();
            loadFiles();
        } else {
            updateFiles();
            showContent();
        }
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        onBackPressed();
        return true;
    }

    private void loadFiles() {
        isLoading = true;
        isError = false;
        String appId = null;
        if (files != null && files.size() > 0) {
            StoreItem lastItem = files.get(files.size() - 1);
            appId = lastItem.getAppId();
        }
        Call<ListResponse> call = serviceHolder.getService().listFiles(1, userId, appId);
        call.enqueue(new Callback<ListResponse>() {
            @Override
            public void onResponse(Call<ListResponse> call, final Response<ListResponse> response) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            onLoaded(response.body());
                        } else {
                            onLoadingError();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<ListResponse> call, Throwable t) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onLoadingError();
                    }
                });
            }
        });
    }

    private void onLoaded(ListResponse body) {
        isLoading = false;
        isError = false;
        if (body.getFiles().isEmpty()) {
            isLoadedAll = true;
        } else {
            updateItemsInstalledVersions(getPackageManager(), body.getFiles());
        }
        if (files == null) {
            files = new ArrayList<>(body.getFiles());
        } else {
            files.addAll(body.getFiles());
        }
        updateFiles();
    }

    private void onLoadingError() {
        isLoading = false;
        isError = true;
        if (files == null) {
            showError();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void updateFiles() {
        adapter.setItems(files);
        adapter.notifyDataSetChanged();
        showContent();
    }

    private void showProgress() {
        viewFlipper.setDisplayedChild(0);
    }

    private void showContent() {
        viewFlipper.setDisplayedChild(1);
    }

    private void showError() {
        errorText.setText(R.string.load_files_error);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                loadFiles();
            }
        });
        viewFlipper.setDisplayedChild(2);
    }

    @Override
    public int onNextPage() {
        if (isError) {
            return FilesListener.STATE_FAILED;
        } else if (isLoading) {
            return FilesListener.STATE_LOADING;
        } else if (isLoadedAll) {
            return FilesListener.STATE_LOADED;
        } else {
            loadFiles();
            return FilesListener.STATE_LOADING;
        }
    }

    @Override
    public void onRetry() {
        loadFiles();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(StoreItem item) {
        Intent intent = new Intent(this, DownloadActivity.class)
                .putExtra(DownloadActivity.STORE_APP_ID, item.getAppId())
                .putExtra(DownloadActivity.STORE_APP_LABEL, LocaleHelper.getLocalizedLabel(item));
        startActivity(intent);
    }

    public static void updateItemsInstalledVersions(PackageManager packageManager,
                                                    List<StoreItem> items) {
        for (StoreItem item : items) {
            item.setInstalledVersionCode(getInstalledVersionCode(
                    item.getPackageName(), packageManager));
        }
    }
}
