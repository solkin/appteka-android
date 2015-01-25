package com.tomclaw.appsend;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Solkin on 10.12.2014.
 */
public class AppInfoAdapter extends RecyclerView.Adapter<AppItem> {

    private List<AppInfo> appInfoList;
    private LayoutInflater inflater;
    private Context context;

    private AppItemClickListener listener;

    public AppInfoAdapter(Context context, List<AppInfo> appInfoList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.appInfoList = appInfoList;
    }

    public AppItemClickListener getListener() {
        return listener;
    }

    public void setListener(AppItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public AppItem onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.app_item, viewGroup, false);
        return new AppItem(view);
    }

    @Override
    public void onBindViewHolder(AppItem appItem, int i) {
        AppInfo appInfo = appInfoList.get(i);
        appItem.bind(context, appInfo, listener);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return appInfoList.size();
    }

    public static interface AppItemClickListener {
        public void onItemClicked(AppInfo appInfo);
    }
}
