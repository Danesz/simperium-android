package com.simperium.android;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.simperium.BuildConfig;
import com.simperium.client.ClientFactory;
import com.simperium.database.DatabaseProvider;
import com.simperium.database.SQLCipherDatabaseWrapper;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.TrustManager;

/**
 * Refactoring as much of the android specific components of the client
 * and decoupling different parts of the API.
 */
public class SecureAndroidClient implements ClientFactory {

    public static final String TAG = "Simperium.SecureAndroidClient";

    protected Context mContext;
    protected DatabaseProvider mDatabaseProvider;
    protected final String mSessionId;

    protected ExecutorService mExecutor;
    protected AsyncHttpClient mHttpClient = AsyncHttpClient.getDefaultInstance();

    public SecureAndroidClient(final Context context){

        mExecutor = AndroidClientHelper.getDefaultThreadExecutor();
        mContext = context;

        String defaultKey = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "key: " + defaultKey);
        }

        SQLiteDatabase.loadLibs(context);

        String dbPath = "/data/data/" + mContext.getPackageName() + "/" +AndroidClientHelper.DEFAULT_DATABASE_NAME;
        //String dbPath = mContext.getDatabasePath(AndroidClientHelper.DEFAULT_DATABASE_NAME).getAbsolutePath();

        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase( dbPath, defaultKey, null);
        mDatabaseProvider = new DatabaseProvider(new SQLCipherDatabaseWrapper(database));

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

}