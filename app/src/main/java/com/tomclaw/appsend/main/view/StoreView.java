package com.tomclaw.appsend.main.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tomclaw.appsend.DownloadActivity;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.BaseItemAdapter;
import com.tomclaw.appsend.main.adapter.holder.StoreItemHolder;
import com.tomclaw.appsend.main.controller.StoreController;
import com.tomclaw.appsend.main.item.BaseItem;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.ColorHelper;
import com.tomclaw.appsend.util.Debouncer;
import com.tomclaw.appsend.util.EdgeChanger;

import java.util.List;

/**
 * Created by ivsolkin on 08.01.17.
 */
public class StoreView extends MainView implements StoreController.StoreCallback, Debouncer.Callback<String> {

    private ViewFlipper viewFlipper;
    private TextView errorText;
    private BaseItemAdapter adapter;
    private String query;
    private Debouncer<String> filterDebouncer = new Debouncer<>(this, 500);

    public StoreView(final Context context) {
        super(context);

        viewFlipper = (ViewFlipper) findViewById(R.id.apps_view_switcher);

        errorText = (TextView) findViewById(R.id.error_text);

        findViewById(R.id.button_retry).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.apps_list_view);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);
        final int toolbarColor = ColorHelper.getAttributedColor(context, R.attr.toolbar_background);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                EdgeChanger.setEdgeGlowColor(recyclerView, toolbarColor);
            }
        });

        BaseItemAdapter.BaseItemClickListener listener = new BaseItemAdapter.BaseItemClickListener() {
            @Override
            public void onItemClicked(final BaseItem item) {
                StoreItem storeItem = (StoreItem) item;
                Intent intent = new Intent(context, DownloadActivity.class);
                intent.putExtra(DownloadActivity.STORE_APP_ID, storeItem.getAppId());
                intent.putExtra(DownloadActivity.STORE_APP_LABEL, storeItem.getLabel());
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
        if (!isAppend) {
            viewFlipper.setDisplayedChild(0);
        }
    }

    @Override
    public void onLoaded(List<BaseItem> list) {
        setItemList(list);
        viewFlipper.setDisplayedChild(1);
    }

    @Override
    public void onError(boolean isAppend) {
        if (isAppend) {
            adapter.notifyDataSetChanged();
        } else {
            errorText.setText(R.string.store_loading_error);
            viewFlipper.setDisplayedChild(2);
        }
    }

    private String getFilter() {
        if (TextUtils.isEmpty(query) || TextUtils.getTrimmedLength(query) == 0) {
            return null;
        }
        return query.trim();
    }
}
