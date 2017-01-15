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
import com.tomclaw.appsend.util.EdgeChanger;

import java.util.List;

/**
 * Created by ivsolkin on 08.01.17.
 */
public class StoreView extends MainView implements StoreController.StoreCallback {

    private ViewFlipper viewFlipper;
    private TextView errorText;
    private RecyclerView recyclerView;
    private BaseItemAdapter adapter;
    private BaseItemAdapter.BaseItemClickListener listener;

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
        recyclerView = (RecyclerView) findViewById(R.id.apps_list_view);
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

        listener = new BaseItemAdapter.BaseItemClickListener() {
            @Override
            public void onItemClicked(final BaseItem item) {
                StoreItem storeItem = (StoreItem) item;
                Intent intent = new Intent(context, DownloadActivity.class);
                intent.putExtra(DownloadActivity.STORE_ITEM, storeItem);
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
        StoreController.getInstance().reload(getContext());
    }

    public void load(StoreItem item) {
        StoreController.getInstance().load(getContext(), item.getAppId());
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
}
