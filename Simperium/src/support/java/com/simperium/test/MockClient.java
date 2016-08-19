/**
 * A nice fake client
 */
package com.simperium.test;

import com.simperium.client.ClientFactory;
import com.simperium.storage.MemoryStore;
import com.simperium.database.DatabaseProvider;
import com.simperium.database.DatabaseInterface;
import android.content.ContentValues;
import android.database.Cursor;

public class MockClient implements ClientFactory {

    public MockAuthProvider authProvider = new MockAuthProvider();
    public MockChannelProvider channelProvider = new MockChannelProvider();

    @Override
    public MockAuthProvider buildAuthProvider(String appId, String appSecret){
        return authProvider;
    }

    @Override
    public MockChannelProvider buildChannelProvider(String appId){
        return channelProvider;
    }

    @Override
    public MemoryStore buildStorageProvider(){
        return new MemoryStore();
    }

    @Override
    public MockGhostStore buildGhostStorageProvider(){
        return new MockGhostStore();
    }

    @Override
    public MockExecutor.Immediate buildExecutor(){
        return MockExecutor.immediate();
    }

    @Override
    public DatabaseProvider buildDatabaseProvider(){
        return new DatabaseProvider(new DatabaseInterface() {
            @Override
            public void execSQL(String sql) throws RuntimeException {

            }

            @Override
            public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
                return null;
            }

            @Override
            public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
                return null;
            }

            @Override
            public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
                return null;
            }

            @Override
            public long insert(String table, String nullColumnHack, ContentValues values) {
                return 0;
            }

            @Override
            public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws RuntimeException {
                return 0;
            }

            @Override
            public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) {
                return 0;
            }

            @Override
            public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
                return 0;
            }

            @Override
            public int delete(String table, String whereClause, String[] whereArgs) {
                return 0;
            }

            @Override
            public void beginTransaction() {

            }

            @Override
            public void endTransaction() {

            }

            @Override
            public void setTransactionSuccessful() {

            }

            @Override
            public Cursor rawQuery(String sql, String[] selectionArgs) {
                return null;
            }

            @Override
            public void setVersion(int version) {

            }

            @Override
            public boolean changePassword(String password) throws RuntimeException {
                return false;
            }
        }
        );
    }

}