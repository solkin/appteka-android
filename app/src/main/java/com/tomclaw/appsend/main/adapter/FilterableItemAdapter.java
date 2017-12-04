package com.tomclaw.appsend.main.adapter;

import android.content.Context;
import android.widget.Filter;
import android.widget.Filterable;

import com.tomclaw.appsend.main.item.BaseItem;
import com.tomclaw.appsend.main.item.CommonItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ivsolkin on 27.01.17.
 */
public class FilterableItemAdapter extends BaseItemAdapter implements Filterable {

    private final List<BaseItem> originalInfoList;

    public FilterableItemAdapter(Context context) {
        super(context);
        this.originalInfoList = new ArrayList<>();
    }

    @Override
    public void setItemsList(List<? extends BaseItem> items) {
        super.setItemsList(items);
        originalInfoList.clear();
        originalInfoList.addAll(items);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<? extends BaseItem> filtered = filterApps(constraint.toString());
                filterResults.values = filtered;
                filterResults.count = filtered.size();
                return filterResults;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clearItemsList();
                if (results.count > 0) {
                    addToItemsList((List<BaseItem>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    private List<BaseItem> filterApps(String query) {
        List<BaseItem> filtered = new ArrayList<>();
        final Locale locale = Locale.getDefault();
        for (BaseItem item : originalInfoList) {
            boolean append = false;
            if (item instanceof CommonItem) {
                CommonItem commonItem = (CommonItem) item;
                if (commonItem.getLabel().toLowerCase(locale).contains(query) ||
                        commonItem.getPackageName().toLowerCase(locale).contains(query)) {
                    append = true;
                }
            } else {
                append = true;
            }
            if (append) {
                filtered.add(item);
            }
        }
        return filtered;
    }
}
