package com.tomclaw.appsend.main.store;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.main.adapter.files.FilesAdapter;
import com.tomclaw.appsend.main.adapter.files.FilesListener;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.profile.list.ListResponse;
import com.tomclaw.appsend.util.LocaleHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tomclaw.appsend.util.PackageHelper.getInstalledVersionCode;

@EFragment
public abstract class BaseStoreFragment extends Fragment implements FilesListener {

    @ViewById
    ViewFlipper viewFlipper;

    @ViewById
    SwipeRefreshLayout swipeRefresh;

    @ViewById
    RecyclerView recycler;

    @ViewById
    TextView errorText;

    @ViewById
    Button buttonRetry;

    @InstanceState
    ArrayList<StoreItem> files;

    @InstanceState
    boolean isError;

    @InstanceState
    boolean isLoading;

    @InstanceState
    boolean isLoadedAll;

    private FilesAdapter adapter;

    @AfterViews
    void init() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL,
                false);
        adapter = new FilesAdapter(getContext());
        adapter.setHasStableIds(true);
        adapter.setListener(this);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                invalidate();
                loadFiles(true);
            }
        });

        if (files == null) {
            showProgress();
            loadFiles(false);
        } else {
            updateFiles();
            showContent();
        }
    }

    abstract Call<ListResponse> createCall(String appId);

    private void loadFiles(final boolean isInvalidate) {
        isLoading = true;
        isError = false;
        String appId = null;
        if (files != null && files.size() > 0) {
            StoreItem lastItem = files.get(files.size() - 1);
            appId = lastItem.getAppId();
        }
        Call<ListResponse> call = createCall(appId);
        call.enqueue(new Callback<ListResponse>() {
            @Override
            public void onResponse(Call<ListResponse> call, final Response<ListResponse> response) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            ListResponse body = response.body();
                            if (body != null) {
                                onLoaded(body, isInvalidate);
                            }
                            return;
                        }
                        onLoadingError();
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

    private void onLoaded(ListResponse body, boolean isInvalidate) {
        isLoading = false;
        isError = false;
        if (body.getFiles().isEmpty()) {
            isLoadedAll = true;
        } else {
            updateItemsInstalledVersions(getContext().getPackageManager(), body.getFiles());
        }
        if (files == null || isInvalidate) {
            files = new ArrayList<>(body.getFiles());
        } else {
            files.addAll(body.getFiles());
        }
        updateFiles();
        swipeRefresh.setRefreshing(false);
    }

    private void onLoadingError() {
        isLoading = false;
        isError = true;
        if (files == null) {
            showError();
        } else {
            adapter.notifyDataSetChanged();
        }
        swipeRefresh.setRefreshing(false);
    }

    private void invalidate() {
        files = null;
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
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                loadFiles(false);
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
            loadFiles(false);
            return FilesListener.STATE_LOADING;
        }
    }

    @Override
    public void onRetry() {
        loadFiles(false);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(StoreItem item) {
        Intent intent = new Intent(getContext(), DownloadActivity.class)
                .putExtra(DownloadActivity.STORE_APP_ID, item.getAppId())
                .putExtra(DownloadActivity.STORE_APP_LABEL, LocaleHelper.getLocalizedLabel(item))
                .putExtra(DownloadActivity.STORE_FINISH_ONLY, true);
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
