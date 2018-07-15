package com.tomclaw.appsend.main.local;

import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
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
import com.tomclaw.appsend.main.adapter.files.FilesAdapter;
import com.tomclaw.appsend.main.adapter.files.FilesListener;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.main.item.StoreItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static com.tomclaw.appsend.util.PackageHelper.getInstalledVersionCode;

@EFragment
public abstract class LocalFragment extends Fragment implements FilesListener<CommonItem> {

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
    ArrayList<CommonItem> files;

    @InstanceState
    boolean isError;

    @InstanceState
    boolean isLoading;

    private FilesAdapter<CommonItem> adapter;

    @AfterViews
    void init() {
        int orientation = VERTICAL;
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), orientation, false);
        DividerItemDecoration itemDecor =
                new DividerItemDecoration(getContext(), orientation);
        adapter = new FilesAdapter<>(getContext(), new CommonItemViewHolderCreator());
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

        if (files == null) {
            showProgress();
            loadFiles();
        } else {
            updateFiles();
            showContent();
        }
    }

    public void loadFiles() {
        isLoading = true;
        isError = false;
        TaskExecutor.getInstance().execute(new ItemsLoadTask(this));
    }

    abstract List<CommonItem> loadItemsSync();

    private void onLoaded(List<CommonItem> items) {
        isLoading = false;
        isError = false;
        if (files == null) {
            files = new ArrayList<>(items);
        } else {
            files.addAll(items);
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

    @Override
    public void onClick(CommonItem item) {
    }

    public static void updateItemsInstalledVersions(PackageManager packageManager,
                                                    List<StoreItem> items) {
        for (StoreItem item : items) {
            item.setInstalledVersionCode(getInstalledVersionCode(
                    item.getPackageName(), packageManager));
        }
    }

    private static class ItemsLoadTask extends WeakObjectTask<LocalFragment> {

        private List<CommonItem> items;

        public ItemsLoadTask(LocalFragment object) {
            super(object);
        }

        @Override
        public void executeBackground() throws Throwable {
            LocalFragment fragment = getWeakObject();
            if (fragment != null) {
                items = fragment.loadItemsSync();
            }
        }

        @Override
        public void onSuccessMain() {
            LocalFragment fragment = getWeakObject();
            if (fragment != null) {
                fragment.onLoaded(items);
            }
        }

        @Override
        public void onFailMain(Throwable ex) {
            LocalFragment fragment = getWeakObject();
            if (fragment != null) {
                fragment.onLoadingError();
            }
        }

    }

}
