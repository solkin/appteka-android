package com.tomclaw.appsend.net.request;

import android.os.Bundle;

import com.tomclaw.appsend.core.Config;
import com.tomclaw.appsend.core.GlobalProvider;
import com.tomclaw.appsend.util.HttpParamsBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by solkin on 23/04/16.
 */
public class PushRequest extends BaseRequest {

    private String cookie;
    private String text;

    public PushRequest() {
    }

    public PushRequest(String cookie, String text) {
        this.cookie = cookie;
        this.text = text;
    }

    @Override
    protected String getApiName() {
        return "chat/push";
    }

    @Override
    protected void appendParams(HttpParamsBuilder builder) {
        builder.appendParam("cookie", cookie);
        builder.appendParam("text", text);
    }

    @Override
    protected int parsePacket(int status, JSONObject object) throws JSONException {
        if (status == STATUS_OK) {
            long pushTime = object.getLong("time");
            ArrayList<String> cookies = new ArrayList<>();
            cookies.add(cookie);
            Bundle bundle = new Bundle();
            bundle.putSerializable(GlobalProvider.KEY_COOKIE, cookies);
            bundle.putLong(GlobalProvider.KEY_PUSH_TIME, pushTime);
            getContentResolver().call(Config.MESSAGES_RESOLVER_URI,
                    GlobalProvider.METHOD_UPDATE_PUSH_TIME, null, bundle);
            return REQUEST_DELETE;
        }
        return REQUEST_PENDING;
    }

    @Override
    public boolean isUserBased() {
        return true;
    }
}
