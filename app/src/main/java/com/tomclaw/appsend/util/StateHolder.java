package com.tomclaw.appsend.util;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StateHolder {

    private static StateHolder instance;

    private final File cache;

    private StateHolder(Context context) {
        cache = context.getCacheDir();
    }

    public static void init(Context context) {
        instance = new StateHolder(context);
    }

    public static StateHolder stateHolder() {
        if (instance == null) {
            throw new IllegalStateException("StateHolder must be initialized first");
        }
        return instance;
    }

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Map<String, State> states = new HashMap<>();

    public String putState(State state) {
        String key = UUID.randomUUID().toString();
        states.put(key, state);
        StateWriter stateWriter = new StateWriter(cache, key, state);
        state.setFuture(executor.submit(stateWriter));
        return key;
    }

    @SuppressWarnings("unchecked")
    public <A extends State> A removeState(String key) {
        A state = (A) states.get(key);
        if (state != null) {
            Future<?> future = state.getFuture();
            if (future != null) {
                future.cancel(true);
            }
        } else {
            state = readState(key);
        }
        return state;
    }

    private <A extends State> A readState(String key) {
        return null;
    }

    private static class StateWriter implements Runnable {

        private File dir;
        private String key;
        private State state;

        public StateWriter(File dir, String key, State state) {
            this.dir = dir;
            this.key = key;
            this.state = state;
        }

        @Override
        public void run() {
            File file = new File(dir, key);
            Parcel parcel = null;
            OutputStream stream = null;
            try {
                if (file.createNewFile()) {
                    stream = new FileOutputStream(file);
                    parcel = Parcel.obtain();
                    state.writeToParcel(parcel, 0);
                    byte[] data = parcel.createByteArray();
                    stream.write(data);
                    stream.flush();
                } else {
                    throw new IOException();
                }
            } catch (FileNotFoundException ignored) {
            } catch (IOException ignored) {
                Logger.log("unable to write state " + key);
            } finally {
                if (parcel != null) {
                    parcel.recycle();
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    public static abstract class State implements Parcelable {

        private Future<?> future;

        Future<?> getFuture() {
            return future;
        }

        void setFuture(Future<?> future) {
            this.future = future;
        }
    }

}
