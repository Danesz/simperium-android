package com.simperium.android;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by daniel on 20/08/16.
 */
public interface MigrationInterface {
    public String getDatabaseToMigrate();
    public boolean migrateFromDatabase(@NonNull String databaseName, @NonNull DatabaseType databaseType) throws MigrationException;

    //TODO: refactor and remove
    public Context getMigrationContext();

    public enum DatabaseType {
        DEFAULT, SQLCIPHER
    }
}
