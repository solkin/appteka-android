package com.tomclaw.appsend.main.local;

import android.view.View;

import com.tomclaw.appsend.main.adapter.files.FileViewHolder;
import com.tomclaw.appsend.main.adapter.files.FileViewHolderCreator;
import com.tomclaw.appsend.main.item.CommonItem;

public class CommonItemViewHolderCreator implements FileViewHolderCreator<CommonItem> {

    @Override
    public FileViewHolder<CommonItem> create(View view) {
        return new CommonItemViewHolder(view);
    }

}
