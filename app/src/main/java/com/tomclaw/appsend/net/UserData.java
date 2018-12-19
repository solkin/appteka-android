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
    private String email;
    private String name;

    public UserData() {
        guid = "";
        userId = 0;
        fetchTime = 0;
        role = 0;
        email = null;
        name = null;
    }

    public boolean isRegistered() {
        return !TextUtils.isEmpty(guid);
    }

    void setGuid(String guid) {
        Logger.log("obtained guid: " + guid);
        this.guid = guid;
    }

    void setUserId(long userId) {
        Logger.log("obtained user id: " + userId);
        this.userId = userId;
    }

    private void setFetchTime(long fetchTime) {
        this.fetchTime = fetchTime;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void onFetchSuccess(long fetchTime) {
        setFetchTime(fetchTime);
    }

    public void onRoleUpdated(int role) {
        setFetchTime(role);
    }
}
