package com.simperium.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by daniel on 19/08/16.
 *
 * Wrapper around your datastore to hide the implementation.
 *
 */
public class DatabaseProvider implements DatabaseInterface {

    protected DatabaseInterface mDatabaseImplementation;

    public DatabaseProvider(DatabaseInterface databaseImplementation) {
        mDatabaseImplementation = databaseImplementation;
    }

    @Override
    public void execSQL(String sql) throws RuntimeException {
        mDatabaseImplementation.execSQL(sql);
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return mDatabaseImplementation.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return mDatabaseImplementation.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @Override
    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return mDatabaseImplementation.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return mDatabaseImplementation.insert(table, nullColumnHack, values);
    }

    @Override
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws RuntimeException {
        return mDatabaseImplementation.insertOrThrow(table, nullColumnHack, values);
    }

    @Override
    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) {
        return mDatabaseImplementation.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
    }

    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return mDatabaseImplementation.update(table, values, whereClause, whereArgs);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return mDatabaseImplementation.delete(table, whereClause, whereArgs);
    }

    @Override
    public void beginTransaction() {
        mDatabaseImplementation.beginTransaction();
    }

    @Override
    public void endTransaction() {
        mDatabaseImplementation.endTransaction();
    }

    @Override
    public void setTransactionSuccessful() {
        mDatabaseImplementation.setTransactionSuccessful();
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return mDatabaseImplementation.rawQuery(sql, selectionArgs);
    }

    @Override
    public void setVersion(int version) {
        mDatabaseImplementation.setVersion(version);
    }

    @Override
    public boolean changePassword(String password) throws RuntimeException {
        return mDatabaseImplementation.changePassword(password);
    }

}
