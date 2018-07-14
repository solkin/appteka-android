package com.tomclaw.appsend.main.store;

import android.view.View;

import com.tomclaw.appsend.main.adapter.files.FileViewHolder;
import com.tomclaw.appsend.main.adapter.files.FileViewHolderCreator;
import com.tomclaw.appsend.main.item.StoreItem;

public class StoreFileViewHolderCreator implements FileViewHolderCreator<StoreItem> {

    @Override
    public FileViewHolder<StoreItem> create(View view) {
        return new StoreFileViewHolder(view);
    }

}
