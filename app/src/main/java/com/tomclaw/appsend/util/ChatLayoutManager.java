package com.tomclaw.appsend.util;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Igor on 07.07.2015.
 */
public class ChatLayoutManager extends LinearLayoutManager {

    private DataChangedListener dataChangedListener;

    public ChatLayoutManager(Context context) {
        super(context);
        setReverseLayout(true);
    }

    public void setDataChangedListener(DataChangedListener dataChangedListener) {
        this.dataChangedListener = dataChangedListener;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        if (dataChangedListener != null) {
            dataChangedListener.onDataChanged();
        }
    }

    public interface DataChangedListener {
        void onDataChanged();
    }
}
