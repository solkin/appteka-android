package com.tomclaw.appsend.main.store;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
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
import com.tomclaw.appsend.main.home.HomeFragment;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.profile.list.ListResponse;
import com.tomclaw.appsend.util.LocaleHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static com.tomclaw.appsend.util.PackageHelper.getInstalledVersionCode;

@EFragment
public abstract class BaseStoreFragment extends HomeFragment implements FilesListener<StoreItem> {

    @ViewById
    protected ViewFlipper viewFlipper;

    @ViewById
    protected SwipeRefreshLayout swipeRefresh;

    @ViewById
    protected RecyclerView recycler;

    @ViewById
    protected TextView errorText;

    @ViewById
    protected Button buttonRetry;

    @InstanceState
    protected ArrayList<StoreItem> files;

    @InstanceState
    protected boolean isError;

    @InstanceState
    protected boolean isLoading;

    @InstanceState
    protected boolean isLoadedAll;

    private FilesAdapter<StoreItem> adapter;

    @AfterViews
    protected void init() {
        int orientation = VERTICAL;
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), orientation, false);
        DividerItemDecoration itemDecor =
                new DividerItemDecoration(getContext(), orientation);
        adapter = new FilesAdapter<>(new StoreFileViewHolderCreator(getContext()));
        adapter.setHasStableIds(true);
        adapter.setListener(this);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);
        recycler.addItemDecoration(itemDecor);

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

    public abstract Call<ListResponse> createCall(String appId);

    public void clearFiles() {
        invalidate();
        updateFiles();
    }

    public void loadFiles(final boolean isInvalidate) {
        isLoading = true;
        isError = false;
        String appId = null;
        if (files != null && files.size() > 0 && !isInvalidate) {
            StoreItem lastItem = files.get(files.size() - 1);
            appId = lastItem.getAppId();
        }
        Call<ListResponse> call = createCall(appId);
        if (call == null) {
            onLoadingCancelled();
            return;
        }
        call.enqueue(new LoadCallback(this, isInvalidate));
    }

    private void onLoaded(ListResponse body, boolean isInvalidate, PackageManager packageManager) {
        isLoading = false;
        isError = false;
        if (body.getFiles().isEmpty()) {
            isLoadedAll = true;
        } else {
            updateItemsInstalledVersions(packageManager, body.getFiles());
        }
        if (files == null || isInvalidate) {
            files = new ArrayList<>(body.getFiles());
        } else {
            files.addAll(body.getFiles());
        }
        updateFiles();
        swipeRefresh.setRefreshing(false);
    }

    private void onLoadingCancelled() {
        isLoading = false;
        isError = false;
        swipeRefresh.setRefreshing(false);
        showContent();
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
        files = new ArrayList<>();
    }

    private void updateFiles() {
        adapter.setItems(files);
        adapter.notifyDataSetChanged();
        showContent();
    }

    public void showProgress() {
        viewFlipper.setDisplayedChild(0);
    }

    public void showContent() {
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

    private static class LoadCallback implements Callback<ListResponse> {

        private WeakReference<BaseStoreFragment> weakFragment;
        private boolean isInvalidate;

        private LoadCallback(BaseStoreFragment fragment, boolean isInvalidate) {
            this.weakFragment = new WeakReference<>(fragment);
            this.isInvalidate = isInvalidate;
        }

        @Override
        public void onResponse(Call<ListResponse> call, final Response<ListResponse> response) {
            MainExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    BaseStoreFragment fragment = weakFragment.get();
                    if (fragment != null) {
                        Context context = fragment.getContext();
                        if (context != null) {
                            if (response.isSuccessful()) {
                                ListResponse body = response.body();
                                if (body != null) {
                                    fragment.onLoaded(body, isInvalidate, context.getPackageManager());
                                }
                                return;
                            }
                            fragment.onLoadingError();
                        }
                    }
                }
            });
        }

        @Override
        public void onFailure(Call<ListResponse> call, Throwable t) {
            MainExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    BaseStoreFragment fragment = weakFragment.get();
                    if (fragment != null) {
                        fragment.onLoadingError();
                    }
                }
            });
        }
    }
}
