package com.tomclaw.appsend.main.adapter.files;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.item.BaseItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Igor on 23.10.2017.
 */
public class FilesAdapter<T extends BaseItem> extends RecyclerView.Adapter<FileViewHolder<T>> {

    private final Context context;
    private FileViewHolderCreator<T> viewHolderCreator;
    private final List<T> list;

    private FilesListener<T> listener;

    public FilesAdapter(Context context, FileViewHolderCreator<T> viewHolderCreator) {
        this.context = context;
        this.viewHolderCreator = viewHolderCreator;
        this.list = new ArrayList<>();
    }

    public void setListener(FilesListener<T> listener) {
        this.listener = listener;
    }

    public void setItems(List<T> items) {
        list.clear();
        list.addAll(items);
    }

    @NonNull
    @Override
    public FileViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.store_item, parent, false);
        return viewHolderCreator.create(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder<T> holder, int position) {
        T item = list.get(position);
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
