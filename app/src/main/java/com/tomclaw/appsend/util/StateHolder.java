package com.tomclaw.appsend.util;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StateHolder {

    private static final String STATES_DIR = "states";
    private static StateHolder instance;

    private final File cache;

    private StateHolder(Context context) {
        cache = new File(context.getCacheDir(), STATES_DIR);
        if (!cache.mkdirs()) {
            clearCache();
        }
    }

    private void clearCache() {
        for (File file : cache.listFiles()) {
            file.delete();
        }
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
    public <A extends State> A removeState(String key, Class clazz) {
        A state = (A) states.get(key);
        if (state != null) {
            Future<?> future = state.getFuture();
            if (future != null) {
                future.cancel(true);
            }
        } else {
            state = readState(cache, key, clazz);
        }
        return state;
    }

    private <A extends State> A readState(File dir, String key, Class clazz) {
        File file = new File(dir, key);
        Parcel parcel = null;
        InputStream stream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            stream = new FileInputStream(file);
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, read);
            }
            byte[] data = byteArrayOutputStream.toByteArray();
            parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            return parcel.readParcelable(clazz.getClassLoader());
        } catch (IOException e) {
            Logger.log("unable to read state " + key);
        } finally {
            if (parcel != null) {
                parcel.recycle();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.reset();
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
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
                    parcel.writeParcelable(state, 0);
                    byte[] data = parcel.marshall();
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
