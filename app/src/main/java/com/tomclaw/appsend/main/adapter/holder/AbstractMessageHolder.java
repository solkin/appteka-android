package com.tomclaw.appsend.main.adapter.holder;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.tomclaw.appsend.main.adapter.ChatAdapter;
import com.tomclaw.appsend.main.dto.Message;

/**
 * Created by ivsolkin on 23.06.17.
 */
public abstract class AbstractMessageHolder extends RecyclerView.ViewHolder {

    public AbstractMessageHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(final Message message, final Message prevMessage,
                              ChatAdapter.MessageClickListener clickListener);
}
