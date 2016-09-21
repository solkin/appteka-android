package com.tomclaw.appsend;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Solkin on 10.12.2014.
 */
public class AppInfoAdapter extends RecyclerView.Adapter<AbstractAppItem> implements Filterable {

    private static final int DONATE = 1;
    private static final int APP = 2;
    private static final int APK = 3;

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
    public AbstractAppItem onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == APP) {
            View view = inflater.inflate(R.layout.app_item, viewGroup, false);
            return new AppItem(view);
        } else if (viewType == APK) {
            View view = inflater.inflate(R.layout.apk_item, viewGroup, false);
            return new ApkItem(view);
        } else {
            View view = inflater.inflate(R.layout.donate_item, viewGroup, false);
            return new DonateItem(view);
        }
    }

    @Override
    public void onBindViewHolder(AbstractAppItem appItem, int position) {
        AppInfo appInfo = appInfoList.get(position);
        appItem.bind(context, appInfo, listener);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        boolean donateItem = (appInfoList.get(position).getFlags() & AppInfo.FLAG_DONATE_ITEM) == AppInfo.FLAG_DONATE_ITEM;
        if (donateItem) {
            return DONATE;
        }
        boolean apkItem = (appInfoList.get(position).getFlags() & AppInfo.FLAG_APK_FILE) == AppInfo.FLAG_APK_FILE;
        return apkItem ? APK : APP;
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
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                appInfoList.clear();
                if (results.count > 0) {
                    appInfoList.addAll((List<AppInfo>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    private List<AppInfo> filterApps(String query) {
        List<AppInfo> filtered = new ArrayList<>();
        for (AppInfo appInfo : originalInfoList) {
            // TODO: This code needs rather more polymorphism... But I very need for sleep.
            boolean donateItem = (appInfo.getFlags() & AppInfo.FLAG_DONATE_ITEM) == AppInfo.FLAG_DONATE_ITEM;
            if (donateItem || appInfo.getLabel().toLowerCase().contains(query) ||
                    appInfo.getPackageName().toLowerCase().contains(query)) {
                filtered.add(appInfo);
            }
        }
        return filtered;
    }

    interface AppItemClickListener {
        void onItemClicked(AppInfo appInfo);
    }
}
