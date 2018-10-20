package com.tomclaw.appsend.main.adapter.files;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

public interface FileViewHolderCreator<T> {

    FileViewHolder<T> create(@NonNull ViewGroup parent);

}
