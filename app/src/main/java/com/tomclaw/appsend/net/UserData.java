package com.tomclaw.appsend.net;

import android.text.TextUtils;

import com.tomclaw.appsend.util.Logger;
import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by Igor on 07.07.2015.
 */
public class UserData implements Unobfuscatable {

    private String guid;
    private long userId;
    private long fetchTime;
    private int role;

    public UserData() {
        guid = "";
        userId = 0;
        fetchTime = 0;
        role = 0;
    }

    public boolean isRegistered() {
        return !TextUtils.isEmpty(guid);
    }

    private void setGuid(String guid) {
        Logger.log("obtained guid: " + guid);
        this.guid = guid;
    }

    public void setUserId(long userId) {
        Logger.log("obtained user id: " + guid);
        this.userId = userId;
    }

    private void setFetchTime(long fetchTime) {
        this.fetchTime = fetchTime;
    }

    public String getGuid() {
        return guid;
    }

    public long getUserId() {
        return userId;
    }

    public long getFetchTime() {
        return fetchTime;
    }

    public void onUserRegistered(String guid, long userId) {
        Logger.log("User successfully registered: " + guid + ", ID: " + userId);
        setGuid(guid);
        setUserId(userId);
    }

    public void onFetchSuccess(long fetchTime) {
        setFetchTime(fetchTime);
    }
}
