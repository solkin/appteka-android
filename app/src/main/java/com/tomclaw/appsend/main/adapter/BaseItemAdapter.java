package com.tomclaw.appsend.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tomclaw.appsend.BaseItem;
import com.tomclaw.appsend.R;

import java.util.ArrayList;
import java.util.List;

import static com.tomclaw.appsend.BaseItem.APK_ITEM;
import static com.tomclaw.appsend.BaseItem.APP_ITEM;
import static com.tomclaw.appsend.BaseItem.COUCH_ITEM;
import static com.tomclaw.appsend.BaseItem.DONATE_ITEM;

/**
 * Created by Solkin on 10.12.2014.
 */
public class BaseItemAdapter extends RecyclerView.Adapter<AbstractItem> {

    private final List<BaseItem> itemsList;
    private LayoutInflater inflater;
    private Context context;

    private BaseItemClickListener listener;

    public BaseItemAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.itemsList = new ArrayList<>();
    }

    public void setListener(BaseItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public AbstractItem onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        switch (viewType) {
            case APP_ITEM:
                view = inflater.inflate(R.layout.app_item, viewGroup, false);
                return new AppItem(view);
            case APK_ITEM:
                view = inflater.inflate(R.layout.apk_item, viewGroup, false);
                return new ApkItem(view);
            case DONATE_ITEM:
                view = inflater.inflate(R.layout.donate_item, viewGroup, false);
                return new DonateItem(view);
            case COUCH_ITEM:
                view = inflater.inflate(R.layout.couch_item, viewGroup, false);
                return new CouchItem(view);
            default:
                throw new IllegalStateException("Unsupported item type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(AbstractItem appItem, int position) {
        BaseItem appInfo = itemsList.get(position);
        appItem.bind(context, appInfo, listener);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return itemsList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public void setItemsList(List<? extends BaseItem> items) {
        itemsList.clear();
        itemsList.addAll(items);
    }

//    @Override
//    public Filter getFilter() {
//        return new Filter() {
//            @Override
//            protected FilterResults performFiltering(CharSequence constraint) {
//                FilterResults filterResults = new FilterResults();
//                List<BaseItem> filtered = filterApps(constraint.toString());
//                filterResults.values = filtered;
//                filterResults.count = filtered.size();
//                return filterResults;
//            }
//
//            @Override
//            @SuppressWarnings("unchecked")
//            protected void publishResults(CharSequence constraint, FilterResults results) {
//                itemsList.clear();
//                if (results.count > 0) {
//                    itemsList.addAll((List<BaseItem>) results.values);
//                }
//                notifyDataSetChanged();
//            }
//        };
//    }
//
//    private List<BaseItem> filterApps(String query) {
//        List<BaseItem> filtered = new ArrayList<>();
//        for (BaseItem item : originalInfoList) {
//            // TODO: This code needs rather more polymorphism... But I very need for sleep.
//            boolean donateItem = (item.getType() == DONATE_ITEM);
//            if (donateItem || item.getLabel().toLowerCase().contains(query) ||
//                    item.getPackageName().toLowerCase().contains(query)) {
//                filtered.add(item);
//            }
//        }
//        return filtered;
//    }

    public interface BaseItemClickListener {
        void onItemClicked(BaseItem appInfo);
    }
}
