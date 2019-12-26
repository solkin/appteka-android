package com.tomclaw.appsend.core;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.tomclaw.appsend.main.dto.Message;
import com.tomclaw.appsend.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 4/23/13
 * Time: 12:53 AM
 */
public class GlobalProvider extends ContentProvider {

    // Methods.
    public static final String METHOD_INSERT_MESSAGES = "insert_messages";
    public static final String METHOD_UPDATE_MESSAGES = "update_messages";
    public static final String METHOD_DELETE_MESSAGES = "delete_messages";
    public static final String METHOD_UPDATE_PUSH_TIME = "update_push_time";
    public static final String METHOD_PATCH_HOLE = "path_hole";

    // Table
    public static final String REQUEST_TABLE = "requests";
    public static final String MESSAGES_TABLE = "messages";

    // Fields
    public static final String ROW_AUTO_ID = "_id";

    public static final String REQUEST_TYPE = "request_type";
    public static final String REQUEST_CLASS = "request_class";
    public static final String REQUEST_SESSION = "request_session";
    public static final String REQUEST_PERSISTENT = "request_persistent";
    public static final String REQUEST_STATE = "request_state";
    public static final String REQUEST_BUNDLE = "request_bundle";
    public static final String REQUEST_TAG = "request_tag";

    public static final String MESSAGES_USER_ID = "user_id";
    public static final String MESSAGES_MSG_ID = "msg_id";
    public static final String MESSAGES_PREV_MSG_ID = "prev_msg_id";
    public static final String MESSAGES_TEXT = "text";
    public static final String MESSAGES_TIME = "time";
    public static final String MESSAGES_COOKIE = "cookie";
    public static final String MESSAGES_TYPE = "type";
    public static final String MESSAGES_DIRECTION = "direction";
    public static final String MESSAGES_PUSH_TIME = "push_time";

    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;
    public static final int DIRECTION_SERVICE = 2;

    public static final int MESSAGE_TYPE_PLAIN = 0;
    public static final int MESSAGE_TYPE_JOINED = 1;

    // Database create scripts.
    protected static final String DB_CREATE_REQUEST_TABLE_SCRIPT = "create table " + REQUEST_TABLE + "("
            + ROW_AUTO_ID + " integer primary key autoincrement, " + REQUEST_TYPE + " int, "
            + REQUEST_CLASS + " text, " + REQUEST_SESSION + " text, "
            + REQUEST_PERSISTENT + " int, " + REQUEST_STATE + " int, "
            + REQUEST_BUNDLE + " text, " + REQUEST_TAG + " text" + ");";

    protected static final String DB_CREATE_MESSAGES_TABLE_SCRIPT = "create table " + MESSAGES_TABLE + "("
            + ROW_AUTO_ID + " integer primary key autoincrement, "
            + MESSAGES_USER_ID + " int, "
            + MESSAGES_MSG_ID + " int unique, "
            + MESSAGES_PREV_MSG_ID + " int, "
            + MESSAGES_TEXT + " text, "
            + MESSAGES_TIME + " int, "
            + MESSAGES_COOKIE + " text, "
            + MESSAGES_TYPE + " int, "
            + MESSAGES_DIRECTION + " int, "
            + MESSAGES_PUSH_TIME + " int default 0" + ");";

    public static final int ROW_INVALID = -1;

    public static final String KEY_MESSAGES = "messages";
    public static final String KEY_COOKIE = "cookie";
    public static final String KEY_PUSH_TIME = "push_time";
    public static final String KEY_MSG_ID_FROM = "msg_id_from";
    public static final String KEY_MSG_ID_TILL = "msg_id_till";

    // Database helper object.
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase sqLiteDatabase;

    // URI id.
    private static final int URI_REQUEST = 1;
    private static final int URI_MESSAGES = 2;

    // URI tool instance.
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Config.GLOBAL_AUTHORITY, REQUEST_TABLE, URI_REQUEST);
        uriMatcher.addURI(Config.GLOBAL_AUTHORITY, MESSAGES_TABLE, URI_MESSAGES);
    }

    @Override
    public boolean onCreate() {
        Logger.log("GlobalProvider onCreate");
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String table;
        switch (uriMatcher.match(uri)) {
            case URI_REQUEST:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = ROW_AUTO_ID + " ASC";
                }
                table = REQUEST_TABLE;
                break;
            case URI_MESSAGES:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = ROW_AUTO_ID + " ASC";
                }
                table = MESSAGES_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.query(table, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        Logger.log("getType, " + uri.toString());
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        long rowId = sqLiteDatabase.insert(getTableName(uri), null, values);
        Uri resultUri = ContentUris.withAppendedId(uri, rowId);
        // Notify ContentResolver about data changes.
        getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        int rows = sqLiteDatabase.delete(getTableName(uri), selection, selectionArgs);
        // Notify ContentResolver about data changes.
        if (rows > 0) {
            getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        int rows = sqLiteDatabase.update(getTableName(uri), values, selection, selectionArgs);
        // Notify ContentResolver about data changes.
        if (rows > 0) {
            getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Bundle call(@NonNull String method, String arg, Bundle extras) {
        if (TextUtils.equals(method, METHOD_INSERT_MESSAGES)) {
            ArrayList<Message> messages = (ArrayList<Message>) extras.getSerializable(KEY_MESSAGES);
            if (messages != null && !messages.isEmpty()) {
                try {
                    sqLiteDatabase.beginTransaction();
                    for (Message message : messages) {
                        ContentValues values = message.getContentValues();
                        sqLiteDatabase.insertWithOnConflict(MESSAGES_TABLE,
                                null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                    getContentResolver().notifyChange(Config.MESSAGES_RESOLVER_URI, null);
                } finally {
                    sqLiteDatabase.endTransaction();
                }
            }
        } else if (TextUtils.equals(method, METHOD_DELETE_MESSAGES)) {
            ArrayList<Message> messages = (ArrayList<Message>) extras.getSerializable(KEY_MESSAGES);
            if (messages != null && !messages.isEmpty()) {
                try {
                    sqLiteDatabase.beginTransaction();
                    Collections.sort(messages, new Comparator<Message>() {
                        @Override
                        public int compare(Message o1, Message o2) {
                            return compareLong(o1.getMsgId(), o2.getMsgId());
                        }

                        private int compareLong(long x, long y) {
                            return (x < y) ? -1 : ((x == y) ? 0 : 1);
                        }
                    });
                    for (Message message : messages) {
                        sqLiteDatabase.delete(MESSAGES_TABLE, MESSAGES_MSG_ID + "=\'" + message.getMsgId() + "\'", null);
                        ContentValues values = new ContentValues();
                        values.put(GlobalProvider.MESSAGES_PREV_MSG_ID, message.getPrevMsgId());
                        sqLiteDatabase.update(MESSAGES_TABLE, values, MESSAGES_PREV_MSG_ID + "=\'" + message.getMsgId() + "\'", null);
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                    getContentResolver().notifyChange(Config.MESSAGES_RESOLVER_URI, null);
                } finally {
                    sqLiteDatabase.endTransaction();
                }
            }
        } else if (TextUtils.equals(method, METHOD_UPDATE_MESSAGES)) {
            ArrayList<Message> messages = (ArrayList<Message>) extras.getSerializable(KEY_MESSAGES);
            if (messages != null && !messages.isEmpty()) {
                try {
                    sqLiteDatabase.beginTransaction();
                    for (Message message : messages) {
                        ContentValues values = new ContentValues();
                        values.put(GlobalProvider.MESSAGES_USER_ID, message.getUserId());
                        values.put(GlobalProvider.MESSAGES_MSG_ID, message.getMsgId());
                        values.put(GlobalProvider.MESSAGES_PREV_MSG_ID, message.getPrevMsgId());
                        values.put(GlobalProvider.MESSAGES_TIME, message.getTime());
                        sqLiteDatabase.update(MESSAGES_TABLE, values, MESSAGES_COOKIE + "=\'" + message.getCookie() + "\'", null);
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                    getContentResolver().notifyChange(Config.MESSAGES_RESOLVER_URI, null);
                } finally {
                    sqLiteDatabase.endTransaction();
                }
            }
        } else if (TextUtils.equals(method, METHOD_UPDATE_PUSH_TIME)) {
            ArrayList<String> cookies = (ArrayList<String>) extras.getSerializable(KEY_COOKIE);
            long pushTime = extras.getLong(KEY_PUSH_TIME);
            if (cookies != null && !cookies.isEmpty()) {
                try {
                    sqLiteDatabase.beginTransaction();
                    for (String cookie : cookies) {
                        ContentValues values = new ContentValues();
                        values.put(GlobalProvider.MESSAGES_PUSH_TIME, pushTime);
                        sqLiteDatabase.update(MESSAGES_TABLE, values, MESSAGES_COOKIE + "=\'" + cookie + "\'", null);
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                    getContentResolver().notifyChange(Config.MESSAGES_RESOLVER_URI, null);
                } finally {
                    sqLiteDatabase.endTransaction();
                }
            }
        } else if (TextUtils.equals(method, METHOD_PATCH_HOLE)) {
            long msgIdFrom = extras.getLong(KEY_MSG_ID_FROM);
            long msgIdTill = extras.getLong(KEY_MSG_ID_TILL);
            try {
                sqLiteDatabase.beginTransaction();
                ContentValues values = new ContentValues();
                values.put(GlobalProvider.MESSAGES_PREV_MSG_ID, msgIdFrom);
                sqLiteDatabase.update(MESSAGES_TABLE, values, MESSAGES_MSG_ID + "=\'" + msgIdTill + "\'", null);
                sqLiteDatabase.setTransactionSuccessful();
                getContentResolver().notifyChange(Config.MESSAGES_RESOLVER_URI, null);
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }
        return null;
    }

    private static String getTableName(Uri uri) {
        String table;
        switch (uriMatcher.match(uri)) {
            case URI_REQUEST:
                table = REQUEST_TABLE;
                break;
            case URI_MESSAGES:
                table = MESSAGES_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        return table;
    }

    private ContentResolver getContentResolver() {
        return getContext().getContentResolver();
    }
}
