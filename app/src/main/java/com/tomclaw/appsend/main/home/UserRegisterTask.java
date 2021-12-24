package com.tomclaw.appsend.main.home;

import static com.tomclaw.appsend.core.Config.HOST_URL;

import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.HttpTask;
import com.tomclaw.appsend.dto.UserIcon;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.HttpParamsBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserRegisterTask extends HttpTask {

    public UserRegisterTask() {
        super(HOST_URL + "/api/1/user/create", new HttpParamsBuilder());
    }

    @Override
    protected void onLoaded(JSONObject jsonObject) {
        JSONObject result = jsonObject.optJSONObject("result");
        if (result != null) {
            try {
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
                    Session.getInstance().getUserHolder().onUserRegistered(guid, userId, userIcon, role);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onError() {
    }

}
