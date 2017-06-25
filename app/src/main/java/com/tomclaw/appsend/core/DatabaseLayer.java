package com.tomclaw.appsend.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.tomclaw.appsend.util.QueryBuilder;

/**
 * Created by solkin on 17.06.15.
 */
public interface DatabaseLayer {

    public long insert(Uri uri, ContentValues contentValues);

    public int update(Uri uri, ContentValues contentValues, QueryBuilder queryBuilder);

    public Cursor query(Uri uri, QueryBuilder queryBuilder);

    public int delete(Uri uri, QueryBuilder queryBuilder);
}
