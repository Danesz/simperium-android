package com.simperium.database.encryption;

import android.content.Context;
import android.provider.Settings;

/**
 * Basic encryption, it uses the secureID as a password always, it doesn't care about the "real/changed" password
 *
 * Created by daniel on 20/08/16.
 *
 */
public class BasicEncryptionLogic implements EncryptionLogic {

    private final Context mContext;
    private static String deviceID = null;

    public BasicEncryptionLogic(Context context){
        mContext = context;
        deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);


    }

    @Override
    public String getDatabaseEncryptionKey() {
        return deviceID;
    }

    @Override
    public String applyEncryption(String password) throws EncryptionException {
        if (deviceID != null){
            return deviceID;
        } else {
            throw new EncryptionException("Password can not be null!");
        }
    }

    @Override
    public boolean saveDatabaseEncryptionKey(String key) {
        return true;
    }
}
