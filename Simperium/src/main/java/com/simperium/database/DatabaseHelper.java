package com.simperium.database;

import android.content.Context;
import android.support.annotation.NonNull;

import com.simperium.database.encryption.SecureIDEncrypterLogic;

/**
 * Created by daniel on 19/08/16.
 */
public class DatabaseHelper {

    public static DatabaseProvider provideDefaultEncryptedDatabase(@NonNull Context context, @NonNull String databaseName) {
        return new DatabaseProvider(new SQLCipherDatabaseWrapper(context, databaseName, new SecureIDEncrypterLogic(context)));
    }

    public static DatabaseProvider provideNotEncryptedDatabase(@NonNull Context context, @NonNull String databaseName) {
        return new DatabaseProvider(new SQLiteDatabaseWrapper(context, databaseName));
    }

}
