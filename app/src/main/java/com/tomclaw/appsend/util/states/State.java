package com.tomclaw.appsend.util.states;

import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

import java.util.concurrent.Future;

public abstract class State implements Parcelable, Unobfuscatable {

    private transient Future<?> future;

    public State() {
    }

    Future<?> getFuture() {
        return future;
    }

    void setFuture(Future<?> future) {
        this.future = future;
    }

}
