package com.tomclaw.appsend.main.adapter.files;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

public interface FileViewHolderCreator<T> {

    FileViewHolder<T> create(@NonNull ViewGroup parent);

}
