package com.simperium.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.simperium.BuildConfig;
import com.simperium.Version;
import com.simperium.util.Uuid;

import org.thoughtcrime.ssl.pinning.PinningTrustManager;
import org.thoughtcrime.ssl.pinning.SystemKeyStore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.TrustManager;

/**
 * Created by daniel on 19/08/16.
 */
public class AndroidClientHelper {

    private static final String TAG = "Simperium.AndroidClientHelper";

    public static final String SHARED_PREFERENCES_NAME = "simperium";
    public static final String SESSION_ID_PREFERENCE = "simperium-session-id";
    public static final String DEFAULT_DATABASE_NAME = "simperium-store";
    public static final String WEBSOCKET_URL = "https://api.simperium.com/sock/1/%s/websocket";
    public static final String USER_AGENT_HEADER = "User-Agent";

    public static ExecutorService getDefaultThreadExecutor(){
        int threads = Runtime.getRuntime().availableProcessors();
        if (threads > 1) {
            threads -= 1;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("Using %d cores for executors", threads));
        }

        return Executors.newFixedThreadPool(threads);
    }

    public static SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static String generateSessionID(Context context){
        SharedPreferences preferences = getSharedPreferences(context);
        String sessionToken = null;

        if (preferences.contains(SESSION_ID_PREFERENCE)) {
            try {
                sessionToken = preferences.getString(SESSION_ID_PREFERENCE, null);
            } catch (ClassCastException e) {
                sessionToken = null;
            }
        }

        if (sessionToken == null) {
            sessionToken = Uuid.uuid(6);
            preferences.edit().putString(SESSION_ID_PREFERENCE, sessionToken).commit();
        }

        return String.format("%s-%s", Version.LIBRARY_NAME, sessionToken);
    }

    public static TrustManager buildPinnedTrustManager(Context context) {
        // Pin SSL to Simperium.com SPKI
        return new PinningTrustManager(SystemKeyStore.getInstance(context),
                new String[] { BuildConfig.SIMPERIUM_COM_SPKI }, 0);
    }

}
