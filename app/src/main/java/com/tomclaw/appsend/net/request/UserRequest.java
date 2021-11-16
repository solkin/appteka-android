package com.tomclaw.appsend.net.request;

import android.text.TextUtils;

import com.tomclaw.appsend.dto.UserIcon;
import com.tomclaw.appsend.util.HttpParamsBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
            JSONObject userIconObject = result.getJSONObject("user_icon");
            JSONObject labelObject = userIconObject.getJSONObject("label");
            Map<String, String> label = new HashMap<>();
            Iterator<String> labelIterator = labelObject.keys();
            while (labelIterator.hasNext()) {
                String locale = labelIterator.next();
                label.put(locale, labelObject.getString(locale));
            }
            UserIcon userIcon = new UserIcon(
                    userIconObject.getString("icon"),
                    label,
                    userIconObject.getString("color")
            );
            int role = result.getInt("role");
            if (!TextUtils.isEmpty(guid)) {
                getUserHolder().onUserRegistered(guid, userId, userIcon, role);
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
