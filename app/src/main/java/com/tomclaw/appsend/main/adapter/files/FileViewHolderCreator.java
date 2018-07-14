package com.tomclaw.appsend.main.adapter.files;

import android.view.View;

public interface FileViewHolderCreator<T> {

    FileViewHolder<T> create(View view);

}
