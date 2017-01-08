package com.tomclaw.appsend.main.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

/**
 * Created by ivsolkin on 08.01.17.
 */
public abstract class MainView extends FrameLayout {

    protected View view;
    private WeakReference<ActivityCallback> weakActivityCallback;

    public MainView(Context context) {
        super(context);
        view = LayoutInflater.from(context).inflate(getLayout(), this, false);
        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    protected abstract int getLayout();

    public final void activate(ActivityCallback activityCallback) {
        this.weakActivityCallback = new WeakReference<>(activityCallback);
        activate();
    }

    abstract void activate();

    public abstract void start();

    public abstract void stop();

    public abstract void refresh();

    protected void startActivity(Intent intent) {
        ActivityCallback callback = weakActivityCallback.get();
        if (callback != null) {
            callback.startActivity(intent);
        }
    }

    protected void setRefreshOnResume() {
        ActivityCallback callback = weakActivityCallback.get();
        if (callback != null) {
            callback.setRefreshOnResume();
        }
    }

    public interface ActivityCallback {

        void startActivity(Intent intent);

        void setRefreshOnResume();
    }
}
