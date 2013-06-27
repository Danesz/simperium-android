package com.simperium.client;

import java.util.Map;
/**
 * An interface to allow applications to provide a schema for a bucket and a way
 * instatiate custom BucketObject instances
 */
public interface BucketSchema<T extends Syncable> {
    public abstract String getRemoteName();
    public abstract T build(String key, Map<String,Object>properties);
}
