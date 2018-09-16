package com.tomclaw.appsend.core;

import android.net.Uri;

/**
 * Created by ivsolkin on 23.06.17.
 */
public class Config {

    public static String LOG_TAG = "AppSend";
    public static String HOST_URL = "http://192.168.1.69:8888/appsend-store";
    public static String DB_NAME = "appsend_db";
    public static int DB_VERSION = 1;
    public static String GLOBAL_AUTHORITY = "com.tomclaw.appsend.core.GlobalProvider";
    protected static String URI_PREFIX = "content://" + GLOBAL_AUTHORITY + "/";
    public static Uri REQUEST_RESOLVER_URI = Uri.parse(URI_PREFIX + GlobalProvider.REQUEST_TABLE);
    public static Uri MESSAGES_RESOLVER_URI = Uri.parse(URI_PREFIX + GlobalProvider.MESSAGES_TABLE);

}
