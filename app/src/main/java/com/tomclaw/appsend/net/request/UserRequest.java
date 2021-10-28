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
        return "api/1/user/create";
    }

    @Override
    protected void appendParams(HttpParamsBuilder builder) {
    }

    @Override
    protected int parsePacket(int status, JSONObject object) throws JSONException {
        if (status == STATUS_OK) {
            JSONObject result = object.getJSONObject("result");
            String guid = result.getString("guid");
            long userId = result.getLong("user_id");
            int role = result.getInt("role");
            if (!TextUtils.isEmpty(guid)) {
                getUserHolder().onUserRegistered(guid, userId, role);
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
