package com.tomclaw.appsend.main.controller;

import android.content.Context;

import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.PreferenceHelper;

/**
 * Created by solkin on 27.06.2017.
 */
public class DiscussController extends AbstractController<DiscussController.DiscussCallback> {

    private static class Holder {

        static DiscussController instance = new DiscussController();
    }

    public static DiscussController getInstance() {
        return Holder.instance;
    }

    private Context context;

    public void init(Context context) {
        this.context = context;
    }

    public void resetUnreadCount() {
        setUnreadCount(0);
    }

    public synchronized void incrementUnreadCount(int value) {
        assertControllerInitialized();
        int count = PreferenceHelper.getUnreadCount(context);
        PreferenceHelper.setUnreadCount(context, count + value);
        notifyCountListeners();
    }

    public void setUnreadCount(int count) {
        assertControllerInitialized();
        PreferenceHelper.setUnreadCount(context, count);
        notifyCountListeners();
    }

    private void assertControllerInitialized() {
        if (context == null) {
            throw new IllegalStateException("DiscussController must be initialized first");
        }
    }

    @Override
    protected void onAttached(DiscussCallback callback) {
        notifyCountListeners();
        notifyUserListeners();
    }

    @Override
    protected void onDetached(DiscussCallback callback) {
    }

    private void notifyCountListeners() {
        assertControllerInitialized();
        int count = PreferenceHelper.getUnreadCount(context);
        notifyCountListeners(count);
    }

    private void notifyCountListeners(final int count) {
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<DiscussCallback>() {
                    @Override
                    public void invoke(DiscussCallback callback) {
                        callback.onUnreadCount(count);
                    }
                });
            }
        });
    }

    public void onUserReady() {
        notifyUserListeners();
    }

    public void onIntroClosed() {
        PreferenceHelper.setShowDiscussIntro(context, false);
        notifyUserListeners();
    }

    private void notifyUserListeners() {
        if (PreferenceHelper.isShowDiscussIntro(context)) {
            notifyForIntroListeners();
        } else {
            if (Session.getInstance().getUserData().isRegistered()) {
                notifyForUserReadyListeners();
            } else {
                notifyForUserNotReadyListeners();
            }
        }
    }

    private void notifyForIntroListeners() {
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<DiscussCallback>() {
                    @Override
                    public void invoke(DiscussCallback callback) {
                        callback.onShowIntro();
                    }
                });
            }
        });
    }

    private void notifyForUserReadyListeners() {
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<DiscussCallback>() {
                    @Override
                    public void invoke(DiscussCallback callback) {
                        callback.onUserReady();
                    }
                });
            }
        });
    }

    private void notifyForUserNotReadyListeners() {
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<DiscussCallback>() {
                    @Override
                    public void invoke(DiscussCallback callback) {
                        callback.onUserNotReady();
                    }
                });
            }
        });
    }

    public interface DiscussCallback extends AbstractController.ControllerCallback {

        void onUnreadCount(int count);

        void onShowIntro();

        void onUserNotReady();

        void onUserReady();

    }
}
