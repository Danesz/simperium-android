package com.simperium.android;

/**
 * Created by daniel on 20/08/16.
 */
public class MigrationException extends Exception{
    public MigrationException() { super(); }
    public MigrationException(String message) { super(message); }
    public MigrationException(String message, Throwable cause) { super(message, cause); }
    public MigrationException(Throwable cause) { super(cause); }
}
