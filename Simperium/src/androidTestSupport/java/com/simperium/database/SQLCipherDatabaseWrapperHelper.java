package com.simperium.database;

import android.content.Context;
import android.support.annotation.NonNull;

import com.simperium.database.encryption.EncryptionLogic;

import net.sqlcipher.database.SQLiteDatabase;

/**
 * Created by daniel on 20/08/16.
 */
public class SQLCipherDatabaseWrapperHelper extends SQLCipherDatabaseWrapper {
    public SQLCipherDatabaseWrapperHelper(@NonNull Context context, @NonNull String databaseName) {
        super(context, databaseName);
    }

    public SQLCipherDatabaseWrapperHelper(@NonNull Context context, @NonNull String databaseName, EncryptionLogic encrypterLogic) {
        super(context, databaseName, encrypterLogic);
    }

    public SQLiteDatabase getDatabase(){
        return mDatabase;
    }
}
