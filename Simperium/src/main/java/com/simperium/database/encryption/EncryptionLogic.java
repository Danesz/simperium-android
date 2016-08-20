package com.simperium.database.encryption;

/**
 *
 * Interface to use custom hashing/salting/encrypting logic for your SQLCipher database password
 *
 * Created by daniel on 20/08/16.
 */
public interface EncryptionLogic {
    public String getDatabaseEncryptionKey();
    public String applyEncryption(String password) throws EncryptionException;
    public boolean saveDatabaseEncryptionKey(String key);

}
