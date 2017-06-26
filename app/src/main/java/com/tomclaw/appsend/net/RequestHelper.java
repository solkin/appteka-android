package com.tomclaw.appsend.net;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.tomclaw.appsend.core.Config;
import com.tomclaw.appsend.core.DatabaseLayer;
import com.tomclaw.appsend.core.GlobalProvider;
import com.tomclaw.appsend.net.request.Request;
import com.tomclaw.appsend.net.request.UserRequest;
import com.tomclaw.appsend.util.GsonSingleton;
import com.tomclaw.appsend.util.QueryBuilder;

/**
 * Created by ivsolkin on 26.06.17.
 */
public class RequestHelper {

    public static void requestUserRegistration(DatabaseLayer databaseLayer) {
        UserRequest registerRequest = new UserRequest();
        insertRequest(databaseLayer, Request.REQUEST_TYPE_SHORT, registerRequest);
    }

    private static void insertRequest(DatabaseLayer databaseLayer, int type, boolean isPersistent,
                                      String appSession, Request request) {
        insertRequest(databaseLayer, type, isPersistent, null, appSession, request);
    }

    private static void insertRequest(DatabaseLayer databaseLayer, int type,
                                      Request request) {
        insertRequest(databaseLayer, type, true, null, null, request);
    }

    private static void insertRequest(DatabaseLayer databaseLayer, int type,
                                      String tag, Request request) {
        insertRequest(databaseLayer, type, true, tag, null, request);
    }

    private static void insertRequest(DatabaseLayer databaseLayer, int type, boolean isPersistent,
                                      String tag, String appSession, Request request) {
        // Writing to requests database.
        ContentValues contentValues = new ContentValues();
        contentValues.put(GlobalProvider.REQUEST_TYPE, type);
        contentValues.put(GlobalProvider.REQUEST_CLASS, request.getClass().getName());
        contentValues.put(GlobalProvider.REQUEST_PERSISTENT, isPersistent ? 1 : 0);
        contentValues.put(GlobalProvider.REQUEST_STATE, Request.REQUEST_PENDING);
        if (!TextUtils.isEmpty(appSession)) {
            contentValues.put(GlobalProvider.REQUEST_SESSION, appSession);
        }
        if (!TextUtils.isEmpty(tag)) {
            Cursor cursor = null;
            try {
                // Obtain existing request.
                QueryBuilder queryBuilder = new QueryBuilder()
                        .columnEquals(GlobalProvider.REQUEST_TAG, tag);
                cursor = databaseLayer.query(Config.REQUEST_RESOLVER_URI, queryBuilder);
                // Checking for at least one such download request exist.
                if (cursor == null || cursor.moveToFirst()) {
                    return;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            contentValues.put(GlobalProvider.REQUEST_TAG, tag);
        }
        contentValues.put(GlobalProvider.REQUEST_BUNDLE, GsonSingleton.getInstance().toJson(request));
        databaseLayer.insert(Config.REQUEST_RESOLVER_URI, contentValues);
    }


}
