package com.tomclaw.appsend.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateHolder {

    private static class Holder {

        static StateHolder instance = new StateHolder();
    }

    public static StateHolder getInstance() {
        return Holder.instance;
    }

    private Map<String, State> states = new HashMap<>();

    public String putState(State state) {
        String key = UUID.randomUUID().toString();
        states.put(key, state);
        return key;
    }

    @SuppressWarnings("unchecked")
    public <A extends State> A removeState(String key) {
        return (A) states.get(key);
    }

    public static abstract class State implements Parcelable {
    }

    public static class Field<A extends State> implements Parcelable {

        private final A state;

        public Field(A state) {
            this.state = state;
        }

        public A getState() {
            return state;
        }

        protected Field(Parcel in) {
            String key = in.readString();
            state = StateHolder.getInstance().removeState(key);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            String key = StateHolder.getInstance().putState(state);
            dest.writeString(key);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Field> CREATOR = new Creator<Field>() {
            @Override
            public Field createFromParcel(Parcel in) {
                return new Field(in);
            }

            @Override
            public Field[] newArray(int size) {
                return new Field[size];
            }
        };
    }

}
