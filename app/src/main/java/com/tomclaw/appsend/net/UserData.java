package com.tomclaw.appsend.net;

import android.text.TextUtils;

import com.tomclaw.appsend.util.Logger;
import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by Igor on 07.07.2015.
 */
public class UserData implements Unobfuscatable {

    private String guid;
    private long fetchTime;

    public UserData() {
        guid = "";
        fetchTime = 0;
    }

    public boolean isRegistered() {
        return !TextUtils.isEmpty(guid);
    }

    private void setGuid(String guid) {
        Logger.log("obtained guid: " + guid);
        this.guid = guid;
    }

    private void setFetchTime(long fetchTime) {
        this.fetchTime = fetchTime;
    }

    public String getGuid() {
        return guid;
    }

    public long getFetchTime() {
        return fetchTime;
    }

    public void onUserRegistered(String guid) {
        Logger.log("User successfully registered: " + guid);
        setGuid(guid);
    }

    public void onFetchSuccess(long fetchTime) {
        setFetchTime(fetchTime);
    }
}
