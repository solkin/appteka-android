package com.tomclaw.appsend.main.local;

import android.view.View;

import com.tomclaw.appsend.main.adapter.files.FileViewHolder;
import com.tomclaw.appsend.main.adapter.files.FilesListener;
import com.tomclaw.appsend.main.item.CommonItem;

public class CommonItemViewHolder extends FileViewHolder<CommonItem> {

    public CommonItemViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(CommonItem item, boolean isLast, FilesListener<CommonItem> listener) {

    }

}
