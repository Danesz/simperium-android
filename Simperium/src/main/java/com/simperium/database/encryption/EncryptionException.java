package com.simperium.database.encryption;

/**
 * Created by daniel on 20/08/16.
 */
public class EncryptionException extends Exception {
    public EncryptionException() { super(); }
    public EncryptionException(String message) { super(message); }
    public EncryptionException(String message, Throwable cause) { super(message, cause); }
    public EncryptionException(Throwable cause) { super(cause); }
}
