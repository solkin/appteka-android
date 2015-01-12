package com.tomclaw.appsend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Solkin on 10.12.2014.
 */
public class AppInfoAdapter extends BaseAdapter {

    private List<AppInfo> appInfoList;
    private LayoutInflater inflater;
    private Context context;

    public AppInfoAdapter(Context context, List<AppInfo> appInfoList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.appInfoList = appInfoList;
    }

    @Override
    public int getCount() {
        return appInfoList.size();
    }

    @Override
    public AppInfo getItem(int position) {
        return appInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.app_item, parent, false);
        }
        AppInfo appInfo = getItem(position);
        AppItem appItem = (AppItem) convertView;
        appItem.bind(context, appInfo);
        return appItem;
    }
}
