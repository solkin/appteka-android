package com.tomclaw.appsend.util;

import android.content.Context;
import android.os.Parcelable;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

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

import static com.tomclaw.appsend.AppSend.kryo;

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
            if (!file.delete()) {
                Logger.log("unable to delete state " + file.getName());
            }
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
    public <A extends State> A removeState(String key) {
        A state = (A) states.get(key);
        if (state != null) {
            Future<?> future = state.getFuture();
            if (future != null) {
                future.cancel(true);
            }
        } else {
            state = readState(cache, key);
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    private <A extends State> A readState(File dir, String key) {
        File file = new File(dir, key);
        InputStream stream = null;
        Input input = null;
        try {
            stream = new FileInputStream(file);
            input = new Input(stream);
            return (A) kryo().readClassAndObject(input);
        } catch (IOException ex) {
            Logger.log("unable to read state " + key, ex);
        } finally {
            if (input != null) {
                input.close();
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

        StateWriter(File dir, String key, State state) {
            this.dir = dir;
            this.key = key;
            this.state = state;
        }

        @Override
        public void run() {
            String tempKey = "_" + key;
            File file = new File(dir, tempKey);
            OutputStream stream = null;
            Output output = null;
            try {
                if (file.createNewFile()) {
                    stream = new FileOutputStream(file);
                    output = new Output(stream);
                    kryo().writeClassAndObject(output, state);
                    output.flush();
                    stream.flush();
                } else {
                    throw new IOException();
                }
            } catch (FileNotFoundException ignored) {
            } catch (IOException ex) {
                Logger.log("unable to write state " + key, ex);
            } finally {
                if (output != null) {
                    output.close();
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ignored) {
                    }
                }
                if (!file.renameTo(new File(dir, key))) {
                    Logger.log("unable to finalize state " + key);
                }
            }
        }
    }

    public static abstract class State implements Parcelable {

        private transient Future<?> future;

        protected State() {
        }

        Future<?> getFuture() {
            return future;
        }

        void setFuture(Future<?> future) {
            this.future = future;
        }

    }

}
