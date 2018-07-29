package com.tomclaw.appsend.main.local;

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
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.core.WeakObjectTask;
import com.tomclaw.appsend.main.adapter.files.FileViewHolderCreator;
import com.tomclaw.appsend.main.adapter.files.FilesAdapter;
import com.tomclaw.appsend.main.adapter.files.FilesListener;
import com.tomclaw.appsend.main.home.HomeFragment;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.main.item.StoreItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static com.tomclaw.appsend.util.PackageHelper.getInstalledVersionCode;

@EFragment
abstract class CommonItemFragment<T extends CommonItem>
        extends HomeFragment
        implements FilesListener<T> {

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
    boolean isError;

    @InstanceState
    boolean isLoading;

    @InstanceState
    boolean isRefreshOnResume = false;

    private FilesAdapter<T> adapter;

    @AfterViews
    void init() {
        int orientation = VERTICAL;
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), orientation, false);
        DividerItemDecoration itemDecor =
                new DividerItemDecoration(getContext(), orientation);
        adapter = new FilesAdapter<>(getViewHolderCreator());
        adapter.setHasStableIds(true);
        adapter.setListener(this);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);
        recycler.addItemDecoration(itemDecor);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                invalidate();
                loadFiles();
            }
        });

        if (getFiles() == null) {
            showProgress();
            loadFiles();
        } else {
            updateFiles();
            showContent();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isRefreshOnResume) {
            isRefreshOnResume = false;
            reloadFiles();
        }
    }

    protected abstract List<T> getFiles();

    protected abstract void setFiles(List<T> files);

    protected abstract FileViewHolderCreator<T> getViewHolderCreator();

    public void reloadFiles() {
        showProgress();
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

    private void invalidate() {
        setFiles(null);
    }

    private void updateFiles() {
        adapter.setItems(getFiles());
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

    public void setRefreshOnResume() {
        isRefreshOnResume = true;
    }

    public static void updateItemsInstalledVersions(PackageManager packageManager,
                                                    List<StoreItem> items) {
        for (StoreItem item : items) {
            item.setInstalledVersionCode(getInstalledVersionCode(
                    item.getPackageName(), packageManager));
        }
    }

    private static class ItemsLoadTask<A extends CommonItem> extends WeakObjectTask<CommonItemFragment<A>> {

        private List<A> items;

        ItemsLoadTask(CommonItemFragment<A> object) {
            super(object);
        }

        @Override
        public void executeBackground() throws Throwable {
            CommonItemFragment<A> fragment = getWeakObject();
            if (fragment != null) {
                items = fragment.loadItemsSync();
            }
        }

        @Override
        public void onSuccessMain() {
            CommonItemFragment<A> fragment = getWeakObject();
            if (fragment != null) {
                fragment.onLoaded(items);
            }
        }

        @Override
        public void onFailMain(Throwable ex) {
            CommonItemFragment<A> fragment = getWeakObject();
            if (fragment != null) {
                fragment.onLoadingError();
            }
        }

    }

}
