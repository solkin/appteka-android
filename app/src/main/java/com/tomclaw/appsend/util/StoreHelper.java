package com.tomclaw.appsend.util;

import com.tomclaw.appsend.main.dto.StoreVersion;
import com.tomclaw.appsend.main.item.StoreItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ivsolkin on 17.01.17.
 */
public class StoreHelper {

    public static StoreItem parseStoreItem(JSONObject file) throws JSONException {
        String appId = file.getString("app_id");
        String defLabel = file.getString("def_label");
        int downloads = file.getInt("downloads");
        String icon = file.getString("icon");
        long downloadTime = file.getLong("download_time") * 1000;
        Map<String, String> labels = parseStringMap(file.getJSONObject("labels"));
        String packageName = file.getString("package");
        List<String> permissions = parseStringList(file.getJSONArray("permissions"));
        int sdkVersion = file.getInt("sdk_version");
        String androidVersion = file.getString("android");
        String sha1 = file.getString("sha1");
        long size = file.getLong("size");
        long time = file.getLong("time") * 1000;
        int verCode = file.getInt("ver_code");
        String verName = file.getString("ver_name");
        return new StoreItem(defLabel, labels, icon, appId, packageName, verName, verCode,
                sdkVersion, androidVersion, permissions, size, downloads, downloadTime, time, sha1);
    }

    public static StoreVersion parseStoreVersion(JSONObject version) throws JSONException {
        String appId = version.getString("app_id");
        int downloads = version.getInt("downloads");
        int verCode = version.getInt("ver_code");
        String verName = version.getString("ver_name");
        return new StoreVersion(appId, downloads, verCode, verName);
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
