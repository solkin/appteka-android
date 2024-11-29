package com.tomclaw.appsend.main.store;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static com.tomclaw.appsend.screen.details.DetailsActivityKt.createDetailsActivityIntent;
import static com.tomclaw.appsend.util.PackageHelper.getInstalledVersionCode;
import static com.tomclaw.appsend.util.states.StateHolder.stateHolder;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.main.adapter.files.FilesAdapter;
import com.tomclaw.appsend.main.adapter.files.FilesListener;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.LocaleHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseStoreFragment extends Fragment implements FilesListener<StoreItem> {

    private static final String KEY_FILES = "files";

    protected ViewFlipper viewFlipper;

    protected SwipeRefreshLayout swipeRefresh;

    protected RecyclerView recycler;

    protected TextView errorText;

    protected Button buttonRetry;

    protected ArrayList<StoreItem> files;
    protected boolean isError;
    protected boolean isLoading;
    protected boolean isLoadedAll;

    private FilesAdapter<StoreItem> adapter;

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
            onContentReady();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            String stateKey = savedInstanceState.getString(KEY_FILES);
            if (stateKey != null) {
                StoreItemsState itemsState = stateHolder().removeState(stateKey);
                if (itemsState != null) {
                    files = itemsState.getItems();
                    isError = itemsState.isError();
                    isLoading = itemsState.isLoading();
                    isLoadedAll = itemsState.isLoadedAll();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (files != null) {
            StoreItemsState state = new StoreItemsState(files, isError, isLoading, isLoadedAll);
            String stateKey = stateHolder().putState(state);
            outState.putString(KEY_FILES, stateKey);
        }
    }

    public abstract Call<ApiResponse<ListResponse>> createCall(String appId, int offset);

    public void clearFiles() {
        invalidate();
        updateFiles();
    }

    public void loadFiles(final boolean isInvalidate) {
        isLoading = true;
        isError = false;
        if (isInvalidate) {
            isLoadedAll = false;
        }
        String appId = null;
        int offset = 0;
        if (files != null && files.size() > 0 && !isInvalidate) {
            StoreItem lastItem = files.get(files.size() - 1);
            appId = lastItem.getAppId();
            offset = files.size();
        }
        Call<ApiResponse<ListResponse>> call = createCall(appId, offset);
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
        onContentReady();
    }

    private void onLoadingError(boolean isInvalidate) {
        isLoading = false;
        isError = true;
        if (files == null || isInvalidate) {
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
        onContentReady();
    }

    private void onContentReady() {
        if (files == null || files.isEmpty()) {
            showEmptyView();
        } else {
            showContent();
        }
    }

    public void showProgress() {
        swipeRefresh.setEnabled(false);
        viewFlipper.setDisplayedChild(0);
    }

    public void showContent() {
        swipeRefresh.setEnabled(true);
        viewFlipper.setDisplayedChild(1);
    }

    public void showEmptyView() {
        swipeRefresh.setEnabled(true);
        viewFlipper.setDisplayedChild(2);
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
        swipeRefresh.setEnabled(true);
        viewFlipper.setDisplayedChild(3);
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
        Context context = getContext();
        if (context == null) {
            return;
        }
        String appId = item.getAppId();
        String label = LocaleHelper.getLocalizedLabel(item);
        Intent intent = createDetailsActivityIntent(
                context,
                appId,
                null,
                label,
                false,
                true
        );
        startActivity(intent);
    }

    public static void updateItemsInstalledVersions(PackageManager packageManager,
                                                    List<StoreItem> items) {
        for (StoreItem item : items) {
            item.setInstalledVersionCode(getInstalledVersionCode(
                    item.getPackageName(), packageManager));
        }
    }

    private static class LoadCallback implements Callback<ApiResponse<ListResponse>> {

        private final WeakReference<BaseStoreFragment> weakFragment;
        private final boolean isInvalidate;

        private LoadCallback(BaseStoreFragment fragment, boolean isInvalidate) {
            this.weakFragment = new WeakReference<>(fragment);
            this.isInvalidate = isInvalidate;
        }

        @Override
        public void onResponse(Call<ApiResponse<ListResponse>> call, final Response<ApiResponse<ListResponse>> response) {
            MainExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    BaseStoreFragment fragment = weakFragment.get();
                    if (fragment != null && fragment.isAdded()) {
                        Context context = fragment.getContext();
                        if (context != null) {
                            if (response.isSuccessful()) {
                                ListResponse body = response.body().getResult();
                                if (body != null) {
                                    fragment.onLoaded(body, isInvalidate, context.getPackageManager());
                                }
                                return;
                            }
                            fragment.onLoadingError(isInvalidate);
                        }
                    }
                }
            });
        }

        @Override
        public void onFailure(Call<ApiResponse<ListResponse>> call, Throwable t) {
            MainExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    BaseStoreFragment fragment = weakFragment.get();
                    if (fragment != null && fragment.isAdded()) {
                        fragment.onLoadingError(isInvalidate);
                    }
                }
            });
        }
    }

}
