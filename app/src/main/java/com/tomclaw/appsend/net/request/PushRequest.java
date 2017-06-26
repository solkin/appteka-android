package com.tomclaw.appsend.net.request;

import com.tomclaw.appsend.util.HttpParamsBuilder;

import org.json.JSONException;
import org.json.JSONObject;

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
        return "push";
    }

    @Override
    protected void appendParams(HttpParamsBuilder builder) {
        builder.appendParam("cookie", cookie);
        builder.appendParam("text", text);
    }

    @Override
    protected int parsePacket(int status, JSONObject object) throws JSONException {
        if (status == STATUS_OK) {
            return REQUEST_DELETE;
        }
        return REQUEST_PENDING;
    }

    @Override
    public boolean isUserBased() {
        return true;
    }
}
