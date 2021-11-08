package com.tomclaw.appsend.core;

import android.net.Uri;

/**
 * Created by ivsolkin on 23.06.17.
 */
public class Config {

    public static String LOG_TAG = "Appteka";
    public static final String HOST_URL = "https://appteka.store";
    public static final String STATUS_HOST_URL = "https://tomclaw.com/api/appteka/status.php";
    public static String DB_NAME = "appsend_db";
    public static int DB_VERSION = 1;
    public static String GLOBAL_AUTHORITY = "com.tomclaw.appsend.core.GlobalProvider";
    protected static String URI_PREFIX = "content://" + GLOBAL_AUTHORITY + "/";
    public static Uri REQUEST_RESOLVER_URI = Uri.parse(URI_PREFIX + GlobalProvider.REQUEST_TABLE);
    public static Uri MESSAGES_RESOLVER_URI = Uri.parse(URI_PREFIX + GlobalProvider.MESSAGES_TABLE);

}
