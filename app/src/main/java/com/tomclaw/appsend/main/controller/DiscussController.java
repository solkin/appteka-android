package com.tomclaw.appsend.main.controller;

import android.content.Context;

import com.tomclaw.appsend.core.MainExecutor;
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
        notifyListeners();
    }

    public void setUnreadCount(int count) {
        assertControllerInitialized();
        PreferenceHelper.setUnreadCount(context, count);
        notifyListeners();
    }

    private void assertControllerInitialized() {
        if (context == null) {
            throw new IllegalStateException("DiscussController must be initialized first");
        }
    }

    @Override
    void onAttached(DiscussCallback callback) {
        notifyListeners();
    }

    @Override
    void onDetached(DiscussCallback callback) {

    }

    private void notifyListeners() {
        assertControllerInitialized();
        int count = PreferenceHelper.getUnreadCount(context);
        notifyListeners(count);
    }

    private void notifyListeners(final int count) {
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

    public interface DiscussCallback extends AbstractController.ControllerCallback {

        void onUnreadCount(int count);

    }
}
