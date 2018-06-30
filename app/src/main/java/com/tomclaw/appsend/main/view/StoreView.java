package com.tomclaw.appsend.main.view;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.flurry.android.FlurryAgent;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.BaseItemAdapter;
import com.tomclaw.appsend.main.adapter.holder.StoreItemHolder;
import com.tomclaw.appsend.main.controller.StoreController;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.item.BaseItem;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.local.LocalAppsActivity;
import com.tomclaw.appsend.main.local.LocalAppsActivity_;
import com.tomclaw.appsend.util.ColorHelper;
import com.tomclaw.appsend.util.Debouncer;
import com.tomclaw.appsend.util.EdgeChanger;
import com.tomclaw.appsend.util.LocaleHelper;

import java.util.List;

/**
 * Created by ivsolkin on 08.01.17.
 */
public class StoreView extends MainView implements StoreController.StoreCallback, Debouncer.Callback<String> {

    private ViewFlipper viewFlipper;
    private SwipeRefreshLayout swipeRefresh;
    private TextView errorText;
    private BaseItemAdapter adapter;
    private String query;
    private Debouncer<String> filterDebouncer = new Debouncer<>(this, 500);

    public StoreView(final Context context) {
        super(context);

        viewFlipper = findViewById(R.id.apps_view_switcher);

        errorText = findViewById(R.id.error_text);

        FloatingActionButton actionButton = findViewById(R.id.fab);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalAppsActivity_.intent(context).start();
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) actionButton.getLayoutParams();
            p.setMargins(0, 0, 0, 0);
            actionButton.setLayoutParams(p);
        }

        findViewById(R.id.button_retry).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.apps_list_view);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);

        final int toolbarColor = ColorHelper.getAttributedColor(context, R.attr.toolbar_background);
        EdgeChanger.setEdgeGlowColor(recyclerView, toolbarColor, null);

        BaseItemAdapter.BaseItemClickListener listener = new BaseItemAdapter.BaseItemClickListener() {
            @Override
            public void onItemClicked(final BaseItem item) {
                FlurryAgent.logEvent("Open store app");
                StoreItem storeItem = (StoreItem) item;
                Intent intent = new Intent(context, DownloadActivity.class);
                intent.putExtra(DownloadActivity.STORE_APP_ID, storeItem.getAppId());
                intent.putExtra(DownloadActivity.STORE_APP_LABEL, LocaleHelper.getLocalizedLabel(storeItem));
                startActivity(intent);
            }

            @Override
            public void onActionClicked(BaseItem item, String action) {
                if (TextUtils.equals(action, StoreItemHolder.ACTION_RETRY)) {
                    StoreItem storeItem = (StoreItem) item;
                    load(storeItem);
                }
            }
        };

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        adapter = new BaseItemAdapter(context);
        adapter.setListener(listener);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected int getLayout() {
        return R.layout.store_view;
    }

    @Override
    public void activate() {
        if (!StoreController.getInstance().isStarted()) {
            refresh();
        }
    }

    @Override
    public void start() {
        StoreController.getInstance().onAttach(this);
    }

    @Override
    public void stop() {
        StoreController.getInstance().onDetach(this);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void refresh() {
        StoreController.getInstance().reload(getContext(), getFilter());
    }

    @Override
    public boolean isFilterable() {
        return true;
    }

    @Override
    public int getMenu() {
        return R.menu.main_menu;
    }

    @Override
    public void filter(String query) {
        if (!TextUtils.equals(this.query, query)) {
            this.query = query;
            filterDebouncer.call("");
        }
    }

    @Override
    public void call(String key) {
        refresh();
    }

    public void load(StoreItem item) {
        StoreController.getInstance().load(getContext(), item.getAppId(), getFilter());
        adapter.notifyDataSetChanged();
    }

    private void setItemList(List<BaseItem> itemList) {
        adapter.setItemsList(itemList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onProgress(boolean isAppend) {
        if (!isAppend && !swipeRefresh.isRefreshing()) {
            viewFlipper.setDisplayedChild(0);
        }
    }

    @Override
    public void onLoaded(List<BaseItem> list) {
        setItemList(list);
        viewFlipper.setDisplayedChild(1);
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onError(boolean isAppend) {
        if (isAppend) {
            adapter.notifyDataSetChanged();
        } else {
            errorText.setText(R.string.store_loading_error);
            viewFlipper.setDisplayedChild(2);
        }
        swipeRefresh.setRefreshing(false);
    }

    private String getFilter() {
        if (TextUtils.isEmpty(query) || TextUtils.getTrimmedLength(query) == 0) {
            return null;
        }
        return query.trim();
    }
}
