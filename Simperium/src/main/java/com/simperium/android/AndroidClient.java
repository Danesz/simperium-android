package com.simperium.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.simperium.client.ClientFactory;
import com.simperium.database.DatabaseHelper;
import com.simperium.database.DatabaseInterface;
import com.simperium.database.DatabaseProvider;
import com.simperium.database.SQLiteDatabaseWrapper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.TrustManager;

/**
 * Refactoring as much of the android specific components of the client
 * and decoupling different parts of the API.
 */
public class AndroidClient implements ClientFactory, MigrationInterface {

    public static final String TAG = "Simperium.AndroidClient";

    protected Context mContext;
    protected DatabaseProvider mDatabaseProvider;
    protected final String mSessionId;

    protected ExecutorService mExecutor;
    protected AsyncHttpClient mHttpClient = AsyncHttpClient.getDefaultInstance();

    public AndroidClient(Context context){

        mExecutor = AndroidClientHelper.getDefaultThreadExecutor();
        mContext = context;

        mDatabaseProvider = DatabaseHelper.provideNotEncryptedDatabase(mContext, Constants.DEFAULT_DATABASE_NAME);

        mSessionId = AndroidClientHelper.generateSessionID(mContext);

        TrustManager[] trustManagers = new TrustManager[] { AndroidClientHelper.buildPinnedTrustManager(context) };
        mHttpClient.getSSLSocketMiddleware().setTrustManagers(trustManagers);
    }

    @Override
    public AsyncAuthClient buildAuthProvider(String appId, String appSecret){
        return new AsyncAuthClient(mContext, appId, appSecret, mHttpClient);
    }

    @Override
    public WebSocketManager buildChannelProvider(String appId){
        // Simperium Bucket API
        WebSocketManager.ConnectionProvider provider = new AsyncWebSocketProvider(appId, mSessionId, mHttpClient);
        return new WebSocketManager(mExecutor, appId, mSessionId, new QueueSerializer(mDatabaseProvider), provider, mContext);
    }

    @Override
    public PersistentStore buildStorageProvider(){
        return new PersistentStore(mDatabaseProvider);
    }

    @Override
    public DatabaseProvider buildDatabaseProvider() {
        return mDatabaseProvider;
    }

    @Override
    public GhostStore buildGhostStorageProvider(){
        return new GhostStore(mDatabaseProvider);
    }

    @Override
    public Executor buildExecutor(){
        return mExecutor;
    }

    @Override
    public String getDatabaseToMigrate() {
        return Constants.DEFAULT_DATABASE_NAME;
    }

    @Override
    public boolean migrateFromDatabase(@NonNull String databaseName, @NonNull DatabaseType databaseType) throws MigrationException {
        throw new MigrationException("Migration is not supported!");
    }

    @Override
    public Context getMigrationContext() {
        return mContext;
    }
}