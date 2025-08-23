package com.tomclaw.appsend.main.local;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.core.WeakObjectTask;
import com.tomclaw.appsend.main.adapter.files.FileViewHolderCreator;
import com.tomclaw.appsend.main.adapter.files.FilesAdapter;
import com.tomclaw.appsend.main.adapter.files.FilesListener;
import com.tomclaw.appsend.main.item.CommonItem;

import java.util.List;

abstract class CommonItemFragment<T extends CommonItem> extends Fragment implements FilesListener<T> {

    ViewFlipper viewFlipper;

    SwipeRefreshLayout swipeRefresh;

    RecyclerView recycler;

    TextView errorText;

    Button buttonRetry;

    boolean isError;

    boolean isLoading;

    boolean isRefreshOnResume = false;

    private FilesAdapter<T> adapter;

    void init() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        int orientation = RecyclerView.VERTICAL;
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, orientation, false);
        DividerItemDecoration itemDecor = new DividerItemDecoration(context, orientation);
        adapter = new FilesAdapter<>(getViewHolderCreator());
        adapter.setHasStableIds(true);
        adapter.setListener(this);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);
        recycler.addItemDecoration(itemDecor);

        swipeRefresh.setOnRefreshListener(() -> {
            invalidate();
            loadFiles();
        });

        if (getFiles() == null) {
            showProgress();
            loadAttempt();
        } else {
            updateFiles();
            onContentReady();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isRefreshOnResume) {
            isRefreshOnResume = false;
            showProgress();
            loadAttempt();
        }
    }

    protected abstract List<T> getFiles();

    protected abstract void setFiles(List<T> files);

    protected abstract FileViewHolderCreator<T> getViewHolderCreator();

    public void loadAttempt() {
        invalidate();
        loadFiles();
    }

    public void loadFiles() {
        isLoading = true;
        isError = false;
        TaskExecutor.getInstance().execute(new ItemsLoadTask<>(this));
    }

    abstract List<T> loadItemsSync();

    private void onLoaded(List<T> items) {
        isLoading = false;
        isError = false;
        if (getFiles() == null) {
            setFiles(items);
        } else {
            getFiles().addAll(items);
        }
        updateFiles();
        swipeRefresh.setRefreshing(false);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onLoadingError() {
        isLoading = false;
        isError = true;
        if (getFiles() == null) {
            showError();
        } else {
            adapter.notifyDataSetChanged();
        }
        swipeRefresh.setRefreshing(false);
    }

    public void invalidate() {
        setFiles(null);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateFiles() {
        adapter.setItems(getFiles());
        adapter.notifyDataSetChanged();
        onContentReady();
    }

    private void onContentReady() {
        if (getFiles() == null || getFiles().isEmpty()) {
            showEmptyView();
        } else {
            showContent();
        }
    }

    public void showProgress() {
        viewFlipper.setDisplayedChild(0);
    }

    public void showContent() {
        viewFlipper.setDisplayedChild(1);
    }

    public void showEmptyView() {
        viewFlipper.setDisplayedChild(2);
    }

    public void showError() {
        errorText.setText(R.string.load_files_error);
        buttonRetry.setOnClickListener(v -> {
            showProgress();
            loadAttempt();
        });
        viewFlipper.setDisplayedChild(3);
    }

    @Override
    public int onNextPage() {
        if (isError) {
            return FilesListener.STATE_FAILED;
        } else if (isLoading) {
            return FilesListener.STATE_LOADING;
        } else {
            loadFiles();
            return FilesListener.STATE_LOADING;
        }
    }

    @Override
    @SuppressLint("NotifyDataSetChanged")
    public void onRetry() {
        loadFiles();
        adapter.notifyDataSetChanged();
    }

    private static class ItemsLoadTask<A extends CommonItem> extends WeakObjectTask<CommonItemFragment<A>> {

        private List<A> items;

        ItemsLoadTask(CommonItemFragment<A> object) {
            super(object);
        }

        @Override
        public void executeBackground() {
            CommonItemFragment<A> fragment = getWeakObject();
            if (fragment != null && fragment.isAdded()) {
                items = fragment.loadItemsSync();
            }
        }

        @Override
        public void onSuccessMain() {
            CommonItemFragment<A> fragment = getWeakObject();
            if (fragment != null && fragment.isAdded()) {
                fragment.onLoaded(items);
            }
        }

        @Override
        public void onFailMain(Throwable ex) {
            CommonItemFragment<A> fragment = getWeakObject();
            if (fragment != null && fragment.isAdded()) {
                fragment.onLoadingError();
            }
        }

    }

}
