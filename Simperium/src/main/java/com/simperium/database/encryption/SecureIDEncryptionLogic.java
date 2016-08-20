package com.simperium.database.encryption;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.simperium.android.AndroidClientHelper;

/**
 *
 * Store and load password, mixed with the SecureID
 *
 * Created by daniel on 20/08/16.
 */
public class SecureIDEncryptionLogic implements EncryptionLogic {

    private static final String DB_ENCRYPTION_KEY = "simperium-db-encryption-key";

    private final Context mContext;
    private static String deviceID = null;

    public SecureIDEncryptionLogic(Context context){
        mContext = context;
        deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);


    }

    @Override
    public String getDatabaseEncryptionKey() {
        SharedPreferences sharedPreferences = AndroidClientHelper.getSharedPreferences(mContext);

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

    @Override
    public String applyEncryption(String password) throws EncryptionException {
        if (password != null){
            return deviceID + password;
        } else {
            throw new EncryptionException("Password can not be null!");
        }
    }

    @Override
    public boolean saveDatabaseEncryptionKey(String key) {
        AndroidClientHelper.getSharedPreferences(mContext).edit().putString(DB_ENCRYPTION_KEY, key).commit();
        return true;
    }
}
