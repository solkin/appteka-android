package com.tomclaw.appsend.main.controller;

import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.PreferenceHelper;

import static com.tomclaw.appsend.AppSend.app;

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

    public void resetUnreadCount() {
        setUnreadCount(0);
    }

    public synchronized void incrementUnreadCount(int value) {
        int count = PreferenceHelper.getUnreadCount(app());
        PreferenceHelper.setUnreadCount(app(), count + value);
        notifyCountListeners();
    }

    public void setUnreadCount(int count) {
        PreferenceHelper.setUnreadCount(app(), count);
        notifyCountListeners();
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
        int count = PreferenceHelper.getUnreadCount(app());
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
        PreferenceHelper.setShowDiscussIntro(app(), false);
        notifyUserListeners();
    }

    private void notifyUserListeners() {
        if (PreferenceHelper.isShowDiscussIntro(app())) {
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
