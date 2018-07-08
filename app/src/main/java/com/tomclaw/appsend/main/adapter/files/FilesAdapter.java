package com.tomclaw.appsend.main.adapter.files;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.item.StoreItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Igor on 23.10.2017.
 */
public class FilesAdapter extends RecyclerView.Adapter<FileViewHolder> {

    private final Context context;
    private final List<StoreItem> list;

    private FilesListener listener;

    public FilesAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
    }

    public void setListener(FilesListener listener) {
        this.listener = listener;
    }

    public void setItems(List<StoreItem> items) {
        list.clear();
        list.addAll(items);
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.store_item, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        StoreItem item = list.get(position);
        boolean isLast = (list.size() - 1 == position);
        holder.bind(item, isLast, listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
