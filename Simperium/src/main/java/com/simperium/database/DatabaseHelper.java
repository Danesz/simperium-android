package com.simperium.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.simperium.BuildConfig;
import com.simperium.android.AndroidClientHelper;
import com.simperium.android.Constants;
import com.simperium.util.Logger;

import net.sqlcipher.database.SQLiteDatabase;

/**
 * Created by daniel on 19/08/16.
 */
public class DatabaseHelper {

    private static final String DB_ENCRYPTION_KEY = "simperium-db-encryption-key";
    private static final String TAG = "DatabaseHelper";

    private static String deviceID = null;

    @Deprecated
    protected static DatabaseProvider provideEncryptedDatabase(@NonNull String databasePath, @NonNull String password) {
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase( databasePath, password, null);
        return new DatabaseProvider(new SQLCipherDatabaseWrapper(database));
    }

    @Deprecated
    public static DatabaseProvider provideDefaultEncryptedDatabase(@NonNull Context context, @NonNull String databaseName) {
        deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (BuildConfig.DEBUG) {
            Logger.log(TAG, "key: " + deviceID);
        }
        String dbPath = context.getDatabasePath(databaseName).getAbsolutePath();

        return provideEncryptedDatabase(dbPath, getDefaultEncryptedDatabaseEncryptionKey(context));
    }

    @Deprecated
    public static DatabaseProvider provideNotEncryptedDatabase(@NonNull Context context, @NonNull String databaseName) {
        android.database.sqlite.SQLiteDatabase database = context.openOrCreateDatabase(databaseName, 0, null);
        return new DatabaseProvider(new SQLiteDatabaseWrapper(database));
    }

    protected static String getDefaultEncryptedDatabaseEncryptionKey(Context context){
        SharedPreferences sharedPreferences = AndroidClientHelper.getSharedPreferences(context);

        String key = deviceID;

        String password = null;
        if (sharedPreferences.contains(DB_ENCRYPTION_KEY)) {
            try {
                password = sharedPreferences.getString(DB_ENCRYPTION_KEY, null);
            } catch (ClassCastException e) {
                password = null;
            }
        }

        if (password != null && !"".equals(password)){
            key = key + password;
        }

        return key;
    }

    protected static String wrapPassword(String password) throws IllegalArgumentException {
        if (password != null){
            return deviceID + password;
        } else {
            throw new IllegalArgumentException("Password can not be null!");
        }
    }

    public static void saveDatabaseEncryptionKey(@NonNull Context context, String key){
        AndroidClientHelper.getSharedPreferences(context).edit().putString(DB_ENCRYPTION_KEY, key).commit();

    }

}
