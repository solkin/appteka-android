package com.tomclaw.appsend.main.controller;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by ivsolkin on 11.01.17.
 */
public abstract class AbstractController<C extends AbstractController.ControllerCallback> {

    private final Set<C> weakCallbacks = Collections.newSetFromMap(
            new WeakHashMap<C, Boolean>());

    public final void onAttach(C callback) {
        weakCallbacks.add(callback);
        onAttached(callback);
    }

    protected abstract void onAttached(C callback);

    public void detachAll() {
        for (C callback : weakCallbacks) {
            onDetach(callback);
        }
    }

    public final void onDetach(C callback) {
        if (weakCallbacks.remove(callback)) {
            onDetached(callback);
        }
    }

    protected abstract void onDetached(C callback);

    protected void operateCallbacks(CallbackOperation<C> operation) {
        for (C callback : weakCallbacks) {
            operation.invoke(callback);
        }
    }

    public interface CallbackOperation<C> {

        void invoke(C callback);
    }

    public interface ControllerCallback {
    }
}
