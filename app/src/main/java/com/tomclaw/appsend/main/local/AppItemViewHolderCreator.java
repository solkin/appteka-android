package com.tomclaw.appsend.main.local;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.files.FileViewHolder;
import com.tomclaw.appsend.main.adapter.files.FileViewHolderCreator;
import com.tomclaw.appsend.main.item.AppItem;

public class AppItemViewHolderCreator implements FileViewHolderCreator<AppItem> {

    private LayoutInflater inflater;

    public AppItemViewHolderCreator(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public FileViewHolder<AppItem> create(@NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.app_item, parent, false);
        return new AppItemViewHolder(view);
    }

}
