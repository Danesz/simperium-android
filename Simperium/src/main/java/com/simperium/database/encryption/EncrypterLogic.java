package com.simperium.database.encryption;

/**
 * Created by daniel on 20/08/16.
 */
public interface EncrypterLogic {
    public String getDatabaseEncryptionKey();
    public String applyEncryption(String password) throws EncryptionException;
    public boolean saveDatabaseEncryptionKey(String key);

}
