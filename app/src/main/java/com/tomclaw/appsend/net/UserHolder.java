package com.tomclaw.appsend.net;

import android.content.Context;

import com.tomclaw.appsend.util.GsonSingleton;
import com.tomclaw.appsend.util.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Igor on 03.07.2015.
 */
public class UserHolder {

    private static final String STORAGE_FOLDER = "user";
    private static final String USER_FILE = "user.dat";

    private UserData userData;

    private File userFile;

    private final List<UserDataListener> listeners;

    public static UserHolder create(Context context) {
        UserHolder userHolder = new UserHolder(context);
        userHolder.load();
        return userHolder;
    }

    private UserHolder(Context context) {
        listeners = new ArrayList<>();
        userData = new UserData();
        File storage = context.getDir(STORAGE_FOLDER, Context.MODE_PRIVATE);
        userFile = new File(storage, USER_FILE);
    }

    private void load() {
        FileInputStream stream = null;
        try {
            if (!userFile.exists() && !userFile.createNewFile()) {
                throw new IOException();
            }
            stream = new FileInputStream(userFile);
            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            UserData loadedUserData = GsonSingleton.getInstance().fromJson(total.toString(), UserData.class);
            if (loadedUserData != null) {
                userData = loadedUserData;
                notifyListeners();
            }
        } catch (Throwable ex) {
            Logger.log("error while reading user data file", ex);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public void store() {
        FileOutputStream stream = null;
        try {
            if (!userFile.exists() && !userFile.createNewFile()) {
                throw new IOException();
            }
            stream = new FileOutputStream(userFile);
            OutputStreamWriter w = new OutputStreamWriter(stream);
            String json = GsonSingleton.getInstance().toJson(userData);
            w.write(json);
            w.flush();
        } catch (Throwable ex) {
            Logger.log("error while writing user data file", ex);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public void onUserRegistered(String guid, long userId) {
        onUserRegistered(guid, userId, null, null);
    }

    public void onUserRegistered(String guid, long userId, String email, String name) {
        Logger.log("User successfully registered: " + guid + ", ID: " + userId);
        userData.setGuid(guid);
        userData.setUserId(userId);
        userData.setEmail(email);
        userData.setName(name);
        store();
        notifyListeners();
    }

    public UserData getUserData() {
        return userData;
    }

    public void attachListener(UserDataListener listener) {
        listeners.add(listener);
        notifyListener(listener);
    }

    public void removeListener(UserDataListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        try {
            for (UserDataListener listener : listeners) {
                notifyListener(listener);
            }
        } catch (Throwable ex) {
            Logger.log("Error while notifying listeners", ex);
        }
    }

    private void notifyListener(UserDataListener listener) {
        final UserData data = userData;
        if (data != null) {
            listener.onUserDataChanged(data);
        }
    }

}
