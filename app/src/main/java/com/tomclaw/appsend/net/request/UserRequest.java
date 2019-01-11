package com.tomclaw.appsend.net.request;

import android.text.TextUtils;

import com.tomclaw.appsend.util.HttpParamsBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Igor on 07.07.2015.
 */
public class UserRequest extends BaseRequest {

    public UserRequest() {
    }

    @Override
    protected String getApiName() {
        return "user";
    }

    @Override
    protected void appendParams(HttpParamsBuilder builder) {
    }

    @Override
    protected int parsePacket(int status, JSONObject object) throws JSONException {
        if (status == STATUS_OK) {
            String guid = object.getString("guid");
            long userId = object.getLong("user_id");
            if (!TextUtils.isEmpty(guid)) {
                getUserHolder().onUserRegistered(guid, userId);
                return REQUEST_DELETE;
            }
        }
        return REQUEST_PENDING;
    }

    @Override
    public boolean isUserBased() {
        return false;
    }
}
