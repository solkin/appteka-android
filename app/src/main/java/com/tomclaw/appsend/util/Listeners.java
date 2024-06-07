package com.tomclaw.appsend.util;

import android.util.Log;

import com.tomclaw.appsend.core.Config;

import java.util.LinkedList;
import java.util.List;

public class Listeners<D> {

    private final List<Listener<D>> listeners = new LinkedList<>();

    public void attachListener(Listener<D> listener) {
        listeners.add(listener);
        listener.repeat();
    }

    public void removeListener(Listener<D> listener) {
        listeners.remove(listener);
    }

    public void notifyListeners(final D data) {
        try {
            for (Listener<D> listener : listeners) {
                listener.notify(data);
            }
        } catch (Throwable e) {
            log("Error while notifying listeners", e);
        }
    }

    public <E extends Throwable> void notifyListeners(E ex) {
        try {
            for (Listener<D> listener : listeners) {
                listener.notify(ex);
            }
        } catch (Throwable e) {
            log("Error while notifying listeners", e);
        }
    }

    private static void log(String message, Throwable ex) {
        Log.d(Config.LOG_TAG, message, ex);
    }

    public static abstract class Listener<D> {

        private D data;
        private Throwable ex;

        private void notify(D data) {
            if (data != null) {
                this.ex = null;
                this.data = data;
                onDataChanged(this.data);
            }
        }

        private <E extends Throwable> void notify(E ex) {
            if (ex != null) {
                this.ex = ex;
                this.data = null;
                onError(ex);
            }
        }

        private void repeat() {
            notify(data);
            notify(ex);
        }

        public abstract void onDataChanged(D data);

        public abstract <E extends Throwable> void onError(E ex);

    }

}
