package com.tomclaw.appsend.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by solkin on 01/03/14.
 */
public class GsonSingleton {

    private static class Holder {

        static GsonSingleton instance = new GsonSingleton();
    }

    public static GsonSingleton getInstance() {
        return Holder.instance;
    }

    private Gson gson;

    public GsonSingleton() {
        gson = new Gson();
    }

    public Gson getGson() {
        return gson;
    }

    public String toJson(Object object) {
        return gson.toJson(object);
    }

    public <T> T fromJson(String json, Class<T> classOfT)
            throws com.google.gson.JsonSyntaxException {
        return gson.fromJson(json, classOfT);
    }

    public <T> T fromJson(String json, Type typeOfT)
            throws com.google.gson.JsonSyntaxException {
        return gson.fromJson(json, typeOfT);
    }
}
