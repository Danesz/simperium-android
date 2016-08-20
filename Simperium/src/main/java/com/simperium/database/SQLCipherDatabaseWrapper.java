package com.simperium.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.simperium.database.encryption.BasicEncryptionLogic;
import com.simperium.database.encryption.EncryptionLogic;
import com.simperium.database.encryption.EncryptionException;
import com.simperium.util.Logger;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

/**
 * Created by daniel on 19/08/16.
 *
 * Wrapper for net.sqlcipher.database.SQLiteDatabase to be able to use with DatabaseProvider
 *
 */
public class SQLCipherDatabaseWrapper implements DatabaseInterface {

    private static final String TAG = "SQLCipherDatabaseWrapper";
    protected SQLiteDatabase mDatabase;
    protected EncryptionLogic mEncryptionLogic;

    public SQLCipherDatabaseWrapper(@NonNull Context context, @NonNull String databaseName) {
        this(context, databaseName, new BasicEncryptionLogic(context));
    }

    public SQLCipherDatabaseWrapper(@NonNull Context context, @NonNull String databaseName, EncryptionLogic encryptionLogic) {
        mEncryptionLogic = encryptionLogic;

        String defaultKey = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String password = defaultKey;
        if (mEncryptionLogic != null) {
            password = mEncryptionLogic.getDatabaseEncryptionKey();
        }

        //String dbPath = context.getDatabasePath(databaseName).getAbsolutePath();
        String dbPath = "/data/data/" + context.getPackageName() + "/" + databaseName;
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase( dbPath, password, null);

        mDatabase = database;
    }

    @Override
    public void execSQL(String sql) throws RuntimeException {
        mDatabase.execSQL(sql);
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @Override
    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return mDatabase.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return mDatabase.insert(table, nullColumnHack, values);
    }

    @Override
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws RuntimeException {
        return mDatabase.insertOrThrow(table, nullColumnHack, values);
    }

    @Override
    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) {
        return mDatabase.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
    }

    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return mDatabase.update(table, values, whereClause, whereArgs);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return mDatabase.delete(table, whereClause, whereArgs);
    }

    @Override
    public void beginTransaction() {
        mDatabase.beginTransaction();
    }

    @Override
    public void endTransaction() {
        mDatabase.endTransaction();
    }

    @Override
    public void setTransactionSuccessful() {
        mDatabase.setTransactionSuccessful();
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return mDatabase.rawQuery(sql, selectionArgs);
    }

    @Override
    public void setVersion(int version) {
        mDatabase.setVersion(version);
    }

    @Override
    public boolean changePassword(String password) throws RuntimeException {
        try {
            mDatabase.changePassword(hashPassword(password));

            if (mEncryptionLogic != null) {
                mEncryptionLogic.saveDatabaseEncryptionKey(password);
            }

            return true;
        } catch (SQLiteException e) {
            Logger.log(TAG, "Changing password failed!", e);
        } catch (EncryptionException e){
            Logger.log(TAG, "Changing password failed in hashing!", e);
        }
        return false;
    }

    public String hashPassword(String password) throws EncryptionException {
        if (mEncryptionLogic != null) {
            return mEncryptionLogic.applyEncryption(password);
        } else {
            return password;
        }
    }
}
