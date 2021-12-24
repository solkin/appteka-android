package com.tomclaw.appsend.net;

import android.text.TextUtils;

import com.tomclaw.appsend.dto.UserIcon;
import com.tomclaw.appsend.util.LegacyLogger;
import com.tomclaw.appsend.util.Unobfuscatable;

import java.util.Collections;

/**
 * Created by Igor on 07.07.2015.
 */
public class UserData implements Unobfuscatable {

    private String guid;
    private long userId;
    private UserIcon userIcon;
    private int role;
    private String email;
    private String name;

    public UserData() {
        guid = "";
        userId = 0;
        userIcon = new UserIcon("", Collections.emptyMap(), "");
        role = 0;
        email = null;
        name = null;
    }

    public boolean isRegistered() {
        return !TextUtils.isEmpty(guid);
    }

    void setGuid(String guid) {
        LegacyLogger.log("obtained guid: " + guid);
        this.guid = guid;
    }

    void setUserId(long userId) {
        LegacyLogger.log("obtained user id: " + userId);
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public void setUserIcon(UserIcon userIcon) {
        this.userIcon = userIcon;
    }

    public String getGuid() {
        return guid;
    }

    public long getUserId() {
        return userId;
    }

    public UserIcon getUserIcon() {
        return userIcon;
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

    public void onRoleUpdated(int role) {
        setRole(role);
    }
}
