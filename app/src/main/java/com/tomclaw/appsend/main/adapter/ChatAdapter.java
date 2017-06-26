package com.tomclaw.appsend.main.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.Config;
import com.tomclaw.appsend.core.GlobalProvider;
import com.tomclaw.appsend.main.adapter.holder.AbstractMessageHolder;
import com.tomclaw.appsend.main.adapter.holder.IncomingMessageHolder;
import com.tomclaw.appsend.main.adapter.holder.OutgoingMessageHolder;
import com.tomclaw.appsend.main.adapter.holder.ServiceMessageHolder;
import com.tomclaw.appsend.main.dto.Message;
import com.tomclaw.appsend.util.QueryBuilder;

/**
 * Created by solkin on 01.07.15.
 */
public class ChatAdapter extends CursorRecyclerAdapter<AbstractMessageHolder>
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final int ADAPTER_ID = 0x03;

    private LoaderManager loaderManager;

    private Context context;

    private AdapterListener adapterListener = null;

    public ChatAdapter(Context context, LoaderManager loaderManager) {
        super(null);
        this.context = context;
        this.loaderManager = loaderManager;
        loaderManager.initLoader(ADAPTER_ID, null, this);
        setHasStableIds(true);
    }

    @Override
    public void onBindViewHolderCursor(AbstractMessageHolder holder, Cursor cursor) {
        Message message = Message.fromCursor(cursor);
        Message prevMessage = null;
        if (cursor.moveToNext()) {
            prevMessage = Message.fromCursor(cursor);
            cursor.moveToPrevious();
        }
        holder.bind(message, prevMessage);
        if (adapterListener != null) {
            if (message.getMsgId() > 0 && message.getPrevMsgId() > 0) {
                if (prevMessage == null && message.getPrevMsgId() > 0) {
                    adapterListener.onHistoryHole(0, message.getMsgId());
                } else if (prevMessage != null && prevMessage.getMsgId() > 0
                        && message.getPrevMsgId() != prevMessage.getMsgId()) {
                    adapterListener.onHistoryHole(prevMessage.getMsgId(), message.getMsgId());
                }
            }
        }
    }

    @Override
    public AbstractMessageHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        switch (viewType) {
            case GlobalProvider.DIRECTION_INCOMING:
                view = inflater.inflate(R.layout.chat_item_inc_text, viewGroup, false);
                return new IncomingMessageHolder(view);
            case GlobalProvider.DIRECTION_OUTGOING:
                view = inflater.inflate(R.layout.chat_item_out_text, viewGroup, false);
                return new OutgoingMessageHolder(view);
            case GlobalProvider.DIRECTION_SERVICE:
                view = inflater.inflate(R.layout.chat_item_srv_text, viewGroup, false);
                return new ServiceMessageHolder(view);
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        int type;
        try {
            Cursor cursor = getCursor();
            if (cursor == null || !cursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            Message message = Message.fromCursor(cursor);
            type = message.getDirection();
        } catch (Throwable ex) {
            type = 0;
        }
        return type;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        QueryBuilder queryBuilder = new QueryBuilder()
                .descending(GlobalProvider.MESSAGES_TIME).andOrder()
                .descending(GlobalProvider.ROW_AUTO_ID);
        return queryBuilder.createCursorLoader(context, Config.MESSAGES_RESOLVER_URI);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        changeCursor(null);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public void close() {
        loaderManager.destroyLoader(ADAPTER_ID);
    }

    public interface AdapterListener {
        void onHistoryHole(long msgIdFrom, long msgIdTill);
    }
}