package com.tomclaw.appsend.main.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.flurry.android.FlurryAgent;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.Config;
import com.tomclaw.appsend.core.ContentResolverLayer;
import com.tomclaw.appsend.core.DatabaseLayer;
import com.tomclaw.appsend.core.GlobalProvider;
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.core.WeakObjectTask;
import com.tomclaw.appsend.main.adapter.ChatAdapter;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.controller.DiscussController;
import com.tomclaw.appsend.main.dto.Message;
import com.tomclaw.appsend.net.RequestHelper;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.ChatLayoutManager;
import com.tomclaw.appsend.util.ColorHelper;
import com.tomclaw.appsend.util.EdgeChanger;
import com.tomclaw.appsend.util.Logger;
import com.tomclaw.appsend.util.StringUtil;

import java.util.ArrayList;

import static com.tomclaw.appsend.util.KeyboardHelper.hideKeyboard;
import static com.tomclaw.appsend.util.KeyboardHelper.showKeyboard;

/**
 * Created by ivsolkin on 23.06.17.
 */
public class DiscussView extends MainView implements DiscussController.DiscussCallback {

    private ViewFlipper viewFlipper;
    private RecyclerView recyclerView;
    private TextView errorText;
    private EditText messageEdit;
    private ChatAdapter adapter;
    private ChatLayoutManager chatLayoutManager;
    private ChatAdapter.AdapterListener adapterListener;
    private ChatAdapter.MessageClickListener messageClickListener;
    private TaskExecutor taskExecutor;
    private DiscussController discussController = DiscussController.getInstance();

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
                discussController.onIntroClosed();
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
        messageClickListener = new ChatAdapter.MessageClickListener() {
            @Override
            public void onMessageClicked(Message message) {
                showMessageContextMenu(message);
            }
        };

        adapter = new ChatAdapter(context, context.getSupportLoaderManager());
        adapter.setAdapterListener(adapterListener);
        adapter.setMessageClickListener(messageClickListener);
        recyclerView.setAdapter(adapter);
    }

    private void showMessageContextMenu(final Message message) {
        ListAdapter menuAdapter = new MenuAdapter(getContext(), R.array.message_actions_titles, R.array.message_actions_icons);
        new AlertDialog.Builder(getContext())
                .setAdapter(menuAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                FlurryAgent.logEvent("Message menu: reply");
                                messageEdit.setText(getResources().getString(R.string.reply_form, message.getText()));
                                messageEdit.setSelection(messageEdit.length());
                                messageEdit.requestFocus();
                                showKeyboard(getContext());
                                break;
                            }
                            case 1: {
                                FlurryAgent.logEvent("Message menu: report");
                                TaskExecutor.getInstance().execute(new ReportMessageTask(getContext(), message.getMsgId(), new ReportCallback() {
                                    @Override
                                    public void onSuccess() {
                                        hideKeyboard(messageEdit);
                                        Snackbar.make(recyclerView, R.string.message_report_sent, Snackbar.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onFailed() {
                                        hideKeyboard(messageEdit);
                                        Snackbar.make(recyclerView, R.string.error_message_report, Snackbar.LENGTH_LONG).show();
                                    }
                                }));
                                break;
                            }
                        }
                    }
                }).show();
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
        discussController.onAttach(this);
    }

    @Override
    public void stop() {
        discussController.onDetach(this);
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

    @Override
    public int getMenu() {
        return R.menu.main_discuss_menu;
    }

    private void showIntro() {
        hideKeyboard(messageEdit);
        viewFlipper.setDisplayedChild(0);
    }

    private void showProgress() {
        hideKeyboard(messageEdit);
        viewFlipper.setDisplayedChild(1);
    }

    private void showDiscuss() {
        hideKeyboard(messageEdit);
        viewFlipper.setDisplayedChild(2);
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

    @Override
    public void onUnreadCount(int count) {
    }

    @Override
    public void onShowIntro() {
        showIntro();
    }

    @Override
    public void onUserNotReady() {
        showProgress();
    }

    @Override
    public void onUserReady() {
        showDiscuss();
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
                long userId = Session.getInstance().getUserData().getUserId();
                Message message = new Message(userId, text, cookie, GlobalProvider.MESSAGE_TYPE_PLAIN,
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

    private static class ReportMessageTask extends WeakObjectTask<Context> {

        private long msgId;
        private ReportCallback callback;

        public ReportMessageTask(Context context, long msgId, ReportCallback callback) {
            super(context);
            this.msgId = msgId;
            this.callback = callback;
        }

        @Override
        public void executeBackground() throws Throwable {
            Context context = getWeakObject();
            if (context != null) {
                DatabaseLayer databaseLayer = ContentResolverLayer.from(context.getContentResolver());
                RequestHelper.requestReportMessage(databaseLayer, msgId);
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

    abstract class ReportCallback {

        public abstract void onSuccess();

        public abstract void onFailed();
    }
}
