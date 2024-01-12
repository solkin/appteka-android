package com.tomclaw.appsend.util.states;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateHolder {

    private static StateHolder instance;

    private StateHolder() {
    }

    public static void init() {
        instance = new StateHolder();
    }

    public static StateHolder stateHolder() {
        if (instance == null) {
            throw new IllegalStateException("StateHolder must be initialized first");
        }
        return instance;
    }

    private final Map<String, State> states = new HashMap<>();

    public String putState(State state) {
        String key = UUID.randomUUID().toString();
        states.put(key, state);
        return key;
    }

    @SuppressWarnings("unchecked")
    public <A extends State> A removeState(String key) {
        return (A) states.get(key);
    }

}
