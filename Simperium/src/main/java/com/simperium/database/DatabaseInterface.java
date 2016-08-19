package com.simperium.database;


import android.content.ContentValues;


/**
 * Created by daniel on 19/08/16.
 *
 * Interface which makes possible to switch database layer behind DatabaseProvider
 * These methods are called currently in the app.
 * It can be extended in the future if we want to support more methods.
 *
 */
public interface DatabaseInterface {


    //default SQLite methods
    public void execSQL(String sql) throws RuntimeException;

    public android.database.Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy);

    public android.database.Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public android.database.Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public long insert(String table, String nullColumnHack, ContentValues values);

    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws RuntimeException;

    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm);

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs);

    public int delete(String table, String whereClause, String[] whereArgs);

    public void beginTransaction();

    public void endTransaction();

    public void setTransactionSuccessful();

    public android.database.Cursor rawQuery(String sql, String[] selectionArgs);

    public void setVersion(int version);

    //SQLCipher methods
    public boolean changePassword(String password) throws RuntimeException;

}
