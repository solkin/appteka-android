package com.tomclaw.appsend.main.discuss;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ListAdapter;
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
import com.tomclaw.appsend.main.home.HomeFragment;
import com.tomclaw.appsend.main.profile.ProfileActivity_;
import com.tomclaw.appsend.net.RequestHelper;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.ChatLayoutManager;
import com.tomclaw.appsend.util.ColorHelper;
import com.tomclaw.appsend.util.EdgeChanger;
import com.tomclaw.appsend.util.Logger;
import com.tomclaw.appsend.util.StringUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import static com.tomclaw.appsend.util.KeyboardHelper.hideKeyboard;
import static com.tomclaw.appsend.util.KeyboardHelper.showKeyboard;

@EFragment(R.layout.discuss_fragment)
public class DiscussFragment extends HomeFragment implements DiscussController.DiscussCallback {

    @ViewById
    ViewFlipper viewFlipper;

    @ViewById
    RecyclerView recycler;

    @ViewById
    EditText messageEdit;

    private ChatAdapter adapter;
    private TaskExecutor taskExecutor;
    private DiscussController discussController = DiscussController.getInstance();

    @AfterViews
    void init() {
        taskExecutor = TaskExecutor.getInstance();

        ChatLayoutManager chatLayoutManager = new ChatLayoutManager(getContext());
        recycler.setLayoutManager(chatLayoutManager);
        recycler.setHasFixedSize(false);
        recycler.setAdapter(adapter);
        recycler.setItemAnimator(null);
        chatLayoutManager.setDataChangedListener(new ChatLayoutManager.DataChangedListener() {
            @Override
            public void onDataChanged() {
            }
        });

        final int toolbarColor = ColorHelper.getAttributedColor(getContext(), R.attr.toolbar_background);
        EdgeChanger.setEdgeGlowColor(recycler, toolbarColor, null);

        ChatAdapter.AdapterListener adapterListener = new ChatAdapter.AdapterListener() {
            @Override
            public void onHistoryHole(long msgIdFrom, long msgIdTill) {
                Logger.log("History hole between " + msgIdFrom + " and " + msgIdTill);
                DatabaseLayer layer = ContentResolverLayer.from(getContext().getContentResolver());
                RequestHelper.requestHistory(layer, msgIdFrom, msgIdTill);
            }
        };
        ChatAdapter.MessageClickListener messageClickListener = new ChatAdapter.MessageClickListener() {
            @Override
            public void onMessageClicked(Message message) {
                if (message.getDirection() == GlobalProvider.DIRECTION_SERVICE) {
                    FlurryAgent.logEvent("Service message: profile");
                    showUserProfile(message);
                } else {
                    showMessageContextMenu(message);
                }
            }
        };

        adapter = new ChatAdapter(getContext(), getActivity().getSupportLoaderManager());
        adapter.setAdapterListener(adapterListener);
        adapter.setMessageClickListener(messageClickListener);
        recycler.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        discussController.onAttach(this);
    }

    @Override
    public void onStop() {
        discussController.onDetach(this);
        super.onStop();
    }

    @Click(R.id.get_started_button)
    void getStarted() {
        discussController.onIntroClosed();
    }

    @Click(R.id.send_button)
    void sendMessage() {
        onSendMessage();
    }

    private void showUserProfile(Message message) {
        ProfileActivity_.intent(getContext())
                .userId(message.getUserId())
                .start();
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
                                FlurryAgent.logEvent("Message menu: profile");
                                showUserProfile(message);
                                break;
                            }
                            case 2: {
                                FlurryAgent.logEvent("Message menu: report");
                                if (message.getMsgId() > 0) {
                                    TaskExecutor.getInstance().execute(new DiscussFragment.ReportMessageTask(getContext(), message.getMsgId(), new DiscussFragment.ReportCallback() {
                                        @Override
                                        public void onSuccess() {
                                            hideKeyboard(messageEdit);
                                            Snackbar.make(recycler, R.string.message_report_sent, Snackbar.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onFailed() {
                                            hideKeyboard(messageEdit);
                                            Snackbar.make(recycler, R.string.error_message_report, Snackbar.LENGTH_LONG).show();
                                        }
                                    }));
                                }
                                break;
                            }
                        }
                    }
                }).show();
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
        recycler.scrollToPosition(0);
        recycler.requestLayout();
    }

    private void onSendMessage() {
        final String message = messageEdit.getText().toString().trim();
        Logger.log("message = " + message);
        if (!TextUtils.isEmpty(message)) {
            messageEdit.setText("");
            scrollBottom();
            DiscussFragment.MessageCallback callback = new DiscussFragment.MessageCallback() {

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailed() {
                    messageEdit.setText(message);
                    Snackbar.make(recycler, R.string.error_sending_message, Snackbar.LENGTH_LONG).show();
                }
            };
            taskExecutor.execute(new DiscussFragment.SendMessageTask(getContext(), message, callback));
        }
    }

    @Override
    public void onUnreadCount(int count) {
        Logger.log("unread messages: " + count);
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
        private final DiscussFragment.MessageCallback callback;

        SendMessageTask(Context context, String text, DiscussFragment.MessageCallback callback) {
            super(context);
            this.text = text;
            this.callback = callback;
        }

        @Override
        public void executeBackground() {
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
        private DiscussFragment.ReportCallback callback;

        ReportMessageTask(Context context, long msgId, DiscussFragment.ReportCallback callback) {
            super(context);
            this.msgId = msgId;
            this.callback = callback;
        }

        @Override
        public void executeBackground() {
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
