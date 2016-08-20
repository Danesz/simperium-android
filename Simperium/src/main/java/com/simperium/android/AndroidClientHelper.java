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
        return context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static String generateSessionID(Context context){
        SharedPreferences preferences = getSharedPreferences(context);
        String sessionToken = null;

        if (preferences.contains(Constants.SESSION_ID_PREFERENCE)) {
            try {
                sessionToken = preferences.getString(Constants.SESSION_ID_PREFERENCE, null);
            } catch (ClassCastException e) {
                sessionToken = null;
            }
        }

        if (sessionToken == null) {
            sessionToken = Uuid.uuid(6);
            preferences.edit().putString(Constants.SESSION_ID_PREFERENCE, sessionToken).commit();
        }

        return String.format("%s-%s", Version.LIBRARY_NAME, sessionToken);
    }

    public static TrustManager buildPinnedTrustManager(Context context) {
        // Pin SSL to Simperium.com SPKI
        return new PinningTrustManager(SystemKeyStore.getInstance(context),
                new String[] { BuildConfig.SIMPERIUM_COM_SPKI }, 0);
    }

    public static boolean isMigrated(Context context){
        return  getSharedPreferences(context).getBoolean(Constants.SECURE_DATABASE_MIGRATED, false);
    }

    public static void setMigrated(Context context, boolean value){
         getSharedPreferences(context).edit().putBoolean(Constants.SECURE_DATABASE_MIGRATED, value);
    }
}
