package com.tomclaw.appsend.main.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.collection.LongSparseArray;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.Config;
import com.tomclaw.appsend.core.GlobalProvider;
import com.tomclaw.appsend.main.adapter.holder.AbstractMessageHolder;
import com.tomclaw.appsend.main.adapter.holder.IncomingMessageHolder;
import com.tomclaw.appsend.main.adapter.holder.OutgoingMessageHolder;
import com.tomclaw.appsend.main.adapter.holder.ServiceMessageHolder;
import com.tomclaw.appsend.main.dto.Message;
import com.tomclaw.appsend.util.LegacyLogger;
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
    private MessageClickListener messageClickListener = null;

    private LongSparseArray<Long> requestedHoles = new LongSparseArray<>();

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
        holder.bind(message, prevMessage, messageClickListener);
        if (adapterListener != null) {
            if (message.getMsgId() > 0 && message.getPrevMsgId() > 0) {
                if (prevMessage == null && message.getPrevMsgId() > 0) {
                    requestHole(0, message.getMsgId());
                } else if (prevMessage != null && prevMessage.getMsgId() > 0
                        && message.getPrevMsgId() != prevMessage.getMsgId()) {
                    requestHole(prevMessage.getMsgId(), message.getMsgId());
                }
            }
        }
    }

    private void requestHole(long from, long till) {
        Long value = requestedHoles.get(from);
        if (value == null || value != till) {
            LegacyLogger.log("request hole for " + from + "-" + till);
            requestedHoles.put(from, till);
            adapterListener.onHistoryHole(from, till);
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
                throw new IllegalStateException("Invalid view type: " + viewType);
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

    public void setMessageClickListener(MessageClickListener messageClickListener) {
        this.messageClickListener = messageClickListener;
    }

    public void close() {
        loaderManager.destroyLoader(ADAPTER_ID);
    }

    public interface AdapterListener {
        void onHistoryHole(long msgIdFrom, long msgIdTill);
    }

    public interface MessageClickListener {
        void onMessageClicked(Message message);
    }
}