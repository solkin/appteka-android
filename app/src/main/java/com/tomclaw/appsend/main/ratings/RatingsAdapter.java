package com.tomclaw.appsend.main.ratings;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.dto.RatingItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Igor on 23.10.2017.
 */
public class RatingsAdapter extends RecyclerView.Adapter<RatingViewHolder> {

    private final Context context;
    private final List<RatingItem> list;

    private OnNextPageListener listener;

    public RatingsAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
    }

    public void setListener(OnNextPageListener listener) {
        this.listener = listener;
    }

    public void setItems(List<RatingItem> items) {
        list.clear();
        list.addAll(items);
    }

    @Override
    public RatingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rating_item, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RatingViewHolder holder, int position) {
        RatingItem item = list.get(position);
        boolean isLast = (list.size() - 1 == position);
        holder.bind(item, isLast, listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        RatingItem item = list.get(position);
        return item.getRateId();
    }
}
