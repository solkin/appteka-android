package com.tomclaw.appsend.util;

import com.tomclaw.appsend.dto.UserIcon;
import com.tomclaw.appsend.main.item.StoreItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by ivsolkin on 17.01.17.
 */
public class StoreHelper {

    public static StoreItem parseStoreItem(JSONObject file) throws JSONException {
        String appId = file.getString("app_id");
        int fileStatus = file.optInt("file_status");
        String defLabel = file.getString("label");
        int downloads = file.getInt("downloads");
        String icon = file.optString("icon");
        long downloadTime = TimeUnit.SECONDS.toMillis(file.getLong("download_time"));
        Map<String, String> labels = parseStringMap(file.getJSONObject("labels"));
        String packageName = file.getString("package");
        List<String> permissions = parseStringList(file.getJSONArray("permissions"));
        int sdkVersion = file.getInt("sdk_version");
        String androidVersion = file.getString("android");
        String sha1 = file.getString("sha1");
        long size = file.getLong("size");
        long time = TimeUnit.SECONDS.toMillis(file.getLong("time"));
        int verCode = file.getInt("ver_code");
        String verName = file.getString("ver_name");
        long userId = file.getLong("user_id");
        JSONObject userIconObject = file.getJSONObject("user_icon");
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
        float rating = (float) file.optDouble("rating", 0.0);
        String filter = file.optString("filter");
        return new StoreItem(defLabel, labels, icon, appId, fileStatus, packageName, verName, verCode,
                sdkVersion, androidVersion, permissions, size, downloads, downloadTime, time,
                sha1, userId, userIcon, rating, filter);
    }

    public static Map<String, String> parseStringMap(JSONObject object) throws JSONException {
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, (String) object.get(key));
        }
        return map;
    }

    public static List<String> parseStringList(JSONArray array) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int c = 0; c < array.length(); c++) {
            list.add(array.getString(c));
        }
        return list;
    }

}
