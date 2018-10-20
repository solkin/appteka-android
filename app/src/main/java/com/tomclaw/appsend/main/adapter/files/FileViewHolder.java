package com.tomclaw.appsend.main.adapter.files;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by solkin on 03.08.17.
 */
public abstract class FileViewHolder<T> extends RecyclerView.ViewHolder {


    public FileViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(final T item, boolean isLast, final FilesListener<T> listener);

}
