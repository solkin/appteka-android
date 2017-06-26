package com.tomclaw.appsend.main.view;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.Config;
import com.tomclaw.appsend.core.ContentResolverLayer;
import com.tomclaw.appsend.core.DatabaseLayer;
import com.tomclaw.appsend.core.GlobalProvider;
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.core.WeakObjectTask;
import com.tomclaw.appsend.main.adapter.ChatAdapter;
import com.tomclaw.appsend.main.dto.Message;
import com.tomclaw.appsend.net.RequestHelper;
import com.tomclaw.appsend.util.ChatLayoutManager;
import com.tomclaw.appsend.util.ColorHelper;
import com.tomclaw.appsend.util.EdgeChanger;
import com.tomclaw.appsend.util.Logger;
import com.tomclaw.appsend.util.StringUtil;

import java.util.ArrayList;

/**
 * Created by ivsolkin on 23.06.17.
 */
public class DiscussView extends MainView {

    private ViewFlipper viewFlipper;
    private RecyclerView recyclerView;
    private TextView errorText;
    private EditText messageEdit;
    private ChatAdapter adapter;
    private ChatLayoutManager chatLayoutManager;
    private ChatAdapter.AdapterListener adapterListener;
    private TaskExecutor taskExecutor;

    public DiscussView(AppCompatActivity context) {
        super(context);

        taskExecutor = TaskExecutor.getInstance();

        viewFlipper = (ViewFlipper) findViewById(R.id.discuss_view_switcher);

        errorText = (TextView) findViewById(R.id.error_text);

        messageEdit = (EditText) findViewById(R.id.message_edit);

        findViewById(R.id.button_retry).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        findViewById(R.id.get_started_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        findViewById(R.id.send_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendMessage();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.discuss_view);

        chatLayoutManager = new ChatLayoutManager(getContext());
        recyclerView.setLayoutManager(chatLayoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);
        chatLayoutManager.setDataChangedListener(new ChatLayoutManager.DataChangedListener() {
            @Override
            public void onDataChanged() {
//                resetUnreadMessages();
            }
        });

        final int toolbarColor = ColorHelper.getAttributedColor(context, R.attr.toolbar_background);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                EdgeChanger.setEdgeGlowColor(recyclerView, toolbarColor);
            }
        });

        adapterListener = new ChatAdapter.AdapterListener() {
            @Override
            public void onHistoryHole(long msgIdFrom, long msgIdTill) {
                Logger.log("History hole between " + msgIdFrom + " and " + msgIdTill);
                DatabaseLayer layer = ContentResolverLayer.from(getContext().getContentResolver());
                RequestHelper.requestHistory(layer, msgIdFrom, msgIdTill);
            }
        };

        adapter = new ChatAdapter(context, context.getSupportLoaderManager());
        adapter.setAdapterListener(adapterListener);
        recyclerView.setAdapter(adapter);

        viewFlipper.setDisplayedChild(2);
    }

    @Override
    protected int getLayout() {
        return R.layout.discuss_view;
    }

    @Override
    void activate() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public boolean isFilterable() {
        return false;
    }

    public void scrollBottom() {
        recyclerView.scrollToPosition(0);
        recyclerView.requestLayout();
    }

    private void onSendMessage() {
        final String message = messageEdit.getText().toString().trim();
        Logger.log("message = " + message);
        if (!TextUtils.isEmpty(message)) {
            messageEdit.setText("");
            scrollBottom();
            MessageCallback callback = new MessageCallback() {

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailed() {
                    messageEdit.setText(message);
                    Snackbar.make(recyclerView, R.string.error_sending_message, Snackbar.LENGTH_LONG).show();
                }
            };
            taskExecutor.execute(new SendMessageTask(getContext(), message, callback));
        }
    }

    private static class SendMessageTask extends WeakObjectTask<Context> {

        private String text;
        private final MessageCallback callback;

        public SendMessageTask(Context context, String text, MessageCallback callback) {
            super(context);
            this.text = text;
            this.callback = callback;
        }

        @Override
        public void executeBackground() throws Throwable {
            Context context = getWeakObject();
            if (context != null) {
                DatabaseLayer databaseLayer = ContentResolverLayer.from(context.getContentResolver());
                String cookie = StringUtil.generateCookie();
                Message message = new Message(text, cookie, GlobalProvider.MESSAGE_TYPE_PLAIN,
                        GlobalProvider.DIRECTION_OUTGOING);

                ArrayList<Message> messages = new ArrayList<>();
                messages.add(message);

                Bundle messagesBundle = new Bundle();
                messagesBundle.putSerializable(GlobalProvider.KEY_MESSAGES, messages);
                context.getContentResolver().call(Config.MESSAGES_RESOLVER_URI,
                        GlobalProvider.METHOD_INSERT_MESSAGES, null, messagesBundle);
                RequestHelper.requestPushMessage(databaseLayer, cookie, text);
            }
        }

        @Override
        public void onSuccessMain() {
            callback.onSuccess();
        }

        @Override
        public void onFailMain(Throwable ex) {
            callback.onFailed();
        }
    }

    abstract class MessageCallback {

        public abstract void onSuccess();

        public abstract void onFailed();
    }
}
