package com.tomclaw.appsend;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Solkin on 10.12.2014.
 */
public class AppInfoAdapter extends RecyclerView.Adapter<AppItem> implements Filterable {

    private final List<AppInfo> appInfoList;
    private final List<AppInfo> originalInfoList;
    private LayoutInflater inflater;
    private Context context;

    private AppItemClickListener listener;

    public AppInfoAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.appInfoList = new ArrayList<>();
        this.originalInfoList = new ArrayList<>();
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

    public void setAppInfoList(List<AppInfo> appInfos) {
        appInfoList.clear();
        originalInfoList.clear();
        appInfoList.addAll(appInfos);
        originalInfoList.addAll(appInfos);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<AppInfo> filtered = filterApps(constraint.toString());
                filterResults.values = filtered;
                filterResults.count = filtered.size();
                return filterResults;
            }

            @Override
            @SuppressWarnings("uncheked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                appInfoList.clear();
                appInfoList.addAll((List<AppInfo>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    private List<AppInfo> filterApps(String query) {
        List<AppInfo> filtered = new ArrayList<>();
        for (AppInfo appInfo : originalInfoList) {
            if (appInfo.getLabel().toLowerCase().contains(query) ||
                    appInfo.getPackageName().toLowerCase().contains(query)) {
                filtered.add(appInfo);
            }
        }
        return filtered;
    }

    public interface AppItemClickListener {
        void onItemClicked(AppInfo appInfo);
    }
}
