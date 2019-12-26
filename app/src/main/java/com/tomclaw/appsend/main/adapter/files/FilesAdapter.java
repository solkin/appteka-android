package com.tomclaw.appsend.main.adapter.files;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tomclaw.appsend.main.item.BaseItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Igor on 23.10.2017.
 */
public class FilesAdapter<T extends BaseItem> extends RecyclerView.Adapter<FileViewHolder<T>> {

    private FileViewHolderCreator<T> viewHolderCreator;
    private final List<T> list;

    private FilesListener<T> listener;

    public FilesAdapter(FileViewHolderCreator<T> viewHolderCreator) {
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
        return viewHolderCreator.create(parent);
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
