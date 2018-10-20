package com.tomclaw.appsend.main.local;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.files.FileViewHolder;
import com.tomclaw.appsend.main.adapter.files.FileViewHolderCreator;
import com.tomclaw.appsend.main.item.ApkItem;

public class ApkItemViewHolderCreator implements FileViewHolderCreator<ApkItem> {

    private LayoutInflater inflater;

    public ApkItemViewHolderCreator(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public FileViewHolder<ApkItem> create(@NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.apk_item, parent, false);
        return new ApkItemViewHolder(view);
    }

}
