package com.tomclaw.appsend.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.tomclaw.appsend.core.DatabaseLayer;

/**
 * Created with IntelliJ IDEA.
 * User: Solkin
 * Date: 03.10.13
 * Time: 11:09
 */
public class QueryBuilder {

    private StringBuilder select;
    private StringBuilder sort;

    public QueryBuilder() {
        recycle();
    }

    private QueryBuilder expression(String column, String action, Object object) {
        select.append(column).append(action).append("'").append(object).append("'");
        return this;
    }

    private QueryBuilder columnExpression(String column1, String action, String column2) {
        select.append(column1).append(action).append(column2);
        return this;
    }

    private QueryBuilder internal(String column1, String action, String column2) {
        select.append(column1).append(action).append(column2);
        return this;
    }

    public QueryBuilder columnEquals(String column, Object object) {
        return expression(column, "=", object);
    }

    public QueryBuilder columnNotEquals(String column, Object object) {
        return expression(column, "!=", object);
    }

    public QueryBuilder more(String column, Object object) {
        return expression(column, ">", object);
    }

    public QueryBuilder moreColumn(String column1, String column2) {
        return columnExpression(column1, ">", column2);
    }

    public QueryBuilder less(String column, Object object) {
        return expression(column, "<", object);
    }

    public QueryBuilder lessInternal(String column1, String column2) {
        return internal(column1, "<", column2);
    }

    public QueryBuilder moreOrEquals(String column, Object object) {
        return expression(column, ">=", object);
    }

    public QueryBuilder lessOrEquals(String column, Object object) {
        return expression(column, "<=", object);
    }

    public QueryBuilder like(String column, Object object) {
        select.append(column).append(" LIKE ").append("'%").append(object).append("%'");
        return this;
    }

    public QueryBuilder likeIgnoreCase(String column, Object object) {
        select.append("UPPER(").append(column).append(")").append(" LIKE ").append("'%").append(object).append("%'");
        return this;
    }

    public QueryBuilder and() {
        if (!TextUtils.isEmpty(select)) {
            select.append(" AND ");
        }
        return this;
    }

    public QueryBuilder or() {
        if (!TextUtils.isEmpty(select)) {
            select.append(" OR ");
        }
        return this;
    }

    private QueryBuilder sortOrder(String column, String order) {
        sort.append(column).append(order);
        return this;
    }

    public QueryBuilder andOrder() {
        sort.append(", ");
        return this;
    }

    public QueryBuilder ascending(String column) {
        return sortOrder(column, " ASC");
    }

    public QueryBuilder descending(String column) {
        return sortOrder(column, " DESC");
    }

    public QueryBuilder limit(int limit) {
        sort.append(" LIMIT ").append(limit);
        return this;
    }

    public QueryBuilder startComplexExpression() {
        select.append("(");
        return this;
    }

    public QueryBuilder finishComplexExpression() {
        select.append(")");
        return this;
    }

    public Cursor query(ContentResolver contentResolver, Uri uri) {
        return query(contentResolver, uri, null);
    }

    public Cursor query(ContentResolver contentResolver, Uri uri, String[] projection) {
        return contentResolver.query(uri, projection, select.toString(), null, sort.toString());
    }

    public Cursor query(DatabaseLayer databaseLayer, Uri uri) {
        return databaseLayer.query(uri, this);
    }

    public int delete(ContentResolver contentResolver, Uri uri) {
        return contentResolver.delete(uri, select.toString(), null);
    }

    public int delete(DatabaseLayer databaseLayer, Uri uri) {
        return databaseLayer.delete(uri, this);
    }

    public int update(ContentResolver contentResolver, ContentValues contentValues, Uri uri) {
        return contentResolver.update(uri, contentValues, select.toString(), null);
    }

    public int update(DatabaseLayer databaseLayer, ContentValues contentValues, Uri uri) {
        return databaseLayer.update(uri, contentValues, this);
    }

    public Loader<Cursor> createCursorLoader(Context context, Uri uri) {
        return new CursorLoader(context, uri, null, select.toString(), null, sort.toString());
    }

    public QueryBuilder recycle() {
        select = new StringBuilder();
        sort = new StringBuilder();
        return this;
    }

    public String getSelect() {
        return select.toString();
    }

    public String getSort() {
        return sort.toString();
    }
}
