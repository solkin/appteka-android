package com.tomclaw.appsend.core;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.tomclaw.appsend.util.QueryBuilder;

/**
 * Created by solkin on 17.06.15.
 */
public class ContentResolverLayer implements DatabaseLayer {

    private ContentResolver contentResolver;

    private static class Holder {

        static ContentResolverLayer instance = new ContentResolverLayer();
    }

    public static ContentResolverLayer from(ContentResolver contentResolver) {
        Holder.instance.contentResolver = contentResolver;
        return Holder.instance;
    }

    private ContentResolverLayer() {
    }

    @Override
    public long insert(Uri uri, ContentValues contentValues) {
        return ContentUris.parseId(contentResolver.insert(uri, contentValues));
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, QueryBuilder queryBuilder) {
        return queryBuilder.update(contentResolver, contentValues, uri);
    }

    @Override
    public Cursor query(Uri uri, QueryBuilder queryBuilder) {
        return queryBuilder.query(contentResolver, uri);
    }

    @Override
    public int delete(Uri uri, QueryBuilder queryBuilder) {
        return queryBuilder.delete(contentResolver, uri);
    }
}
