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

/**
 * Created by Igor on 03.07.2015.
 */
public class UserHolder {

    private static final String STORAGE_FOLDER = "user";
    private static final String USER_FILE = "user.dat";

    private UserData userData;

    private File userFile;

    public static UserHolder create(Context context) {
        UserHolder userHolder = new UserHolder(context);
        userHolder.load();
        return userHolder;
    }

    private UserHolder(Context context) {
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

    public UserData getUserData() {
        return userData;
    }
}
