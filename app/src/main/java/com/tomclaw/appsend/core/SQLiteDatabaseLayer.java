package com.tomclaw.appsend.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.tomclaw.appsend.util.QueryBuilder;

/**
 * Created by solkin on 17.06.15.
 */
public class SQLiteDatabaseLayer implements DatabaseLayer {

    private SQLiteDatabase sqLiteDatabase;

    private static class Holder {

        static SQLiteDatabaseLayer instance = new SQLiteDatabaseLayer();
    }

    public static SQLiteDatabaseLayer from(SQLiteDatabase sqLiteDatabase) {
        Holder.instance.sqLiteDatabase = sqLiteDatabase;
        return Holder.instance;
    }

    private SQLiteDatabaseLayer() {
    }

    @Override
    public long insert(Uri uri, ContentValues contentValues) {
        return sqLiteDatabase.insert(getTableOfUri(uri), null, contentValues);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, QueryBuilder queryBuilder) {
        return sqLiteDatabase.update(getTableOfUri(uri), contentValues, queryBuilder.getSelect(), null);
    }

    @Override
    public Cursor query(Uri uri, QueryBuilder queryBuilder) {
        return sqLiteDatabase.query(getTableOfUri(uri), null, queryBuilder.getSelect(),
                null, null, null, queryBuilder.getSort());
    }

    @Override
    public int delete(Uri uri, QueryBuilder queryBuilder) {
        return sqLiteDatabase.delete(getTableOfUri(uri), queryBuilder.getSelect(), null);
    }

    private static String getTableOfUri(Uri uri) {
        String uriString = uri.toString();
        // Check for this is app-specific Uri.
        if (!uriString.startsWith(Config.URI_PREFIX)) {
            return null;
        }
        // Trim a little bit to get table.
        return uriString.substring(Config.URI_PREFIX.length());
    }
}
