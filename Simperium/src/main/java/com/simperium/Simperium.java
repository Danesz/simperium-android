package com.simperium;

import android.content.Context;

import com.simperium.android.AndroidClient;
import com.simperium.android.AndroidClientHelper;
import com.simperium.android.MigrationException;
import com.simperium.android.MigrationInterface;
import com.simperium.android.SecureAndroidClient;
import com.simperium.client.AuthException;
import com.simperium.client.AuthProvider;
import com.simperium.client.AuthResponseHandler;
import com.simperium.client.AuthResponseListener;
import com.simperium.client.Bucket;
import com.simperium.client.BucketNameInvalid;
import com.simperium.client.BucketObject;
import com.simperium.client.BucketSchema;
import com.simperium.client.ChannelProvider;
import com.simperium.client.ClientFactory;
import com.simperium.client.GhostStorageProvider;
import com.simperium.client.Syncable;
import com.simperium.client.User;
import com.simperium.database.DatabaseProvider;
import com.simperium.storage.StorageProvider;
import com.simperium.storage.StorageProvider.BucketStore;
import com.simperium.util.AuthUtil;
import com.simperium.util.Logger;

import java.util.concurrent.Executor;

public class Simperium implements User.StatusChangeListener {

    // builds and Android client
    public static Simperium newClient(String appId, String appSecret, Context context){
        if (!AndroidClientHelper.isMigrated(context)) {
            return newClient(appId, appSecret, new com.simperium.android.AndroidClient(context));
        } else {
            return newClient(appId, appSecret, new com.simperium.android.SecureAndroidClient(context));
        }
    }

    public static Simperium newClient(String appId, String appSecret, ClientFactory factory){
        simperiumClient = new Simperium(appId, appSecret, factory);
        return simperiumClient;
    }

    public interface OnUserCreatedListener {
        void onUserCreated(User user);
    }

    public static final String VERSION = Version.NUMBER;
    public static final String CLIENT_ID = Version.NAME;
    public static final int SIGNUP_SIGNIN_REQUEST = 1000;  // The request code

    private String appId;
    private String appSecret;

    private ClientFactory currentFactory;
    private User user;
    private User.StatusChangeListener userListener;
    private static Simperium simperiumClient = null;
    private OnUserCreatedListener onUserCreatedListener;

    protected AuthProvider mAuthProvider;
    protected ChannelProvider mChannelProvider;
    protected StorageProvider mStorageProvider;
    protected DatabaseProvider mDatabaseProvider;
    protected GhostStorageProvider mGhostStorageProvider;
    protected Executor mExecutor;

    public Simperium(String appId, String appSecret, ClientFactory factory){
        this.appId = appId;
        this.appSecret = appSecret;
        this.currentFactory = factory;

        mAuthProvider = factory.buildAuthProvider(appId, appSecret);

        mChannelProvider = factory.buildChannelProvider(appId);

        mStorageProvider = factory.buildStorageProvider();

        mDatabaseProvider = factory.buildDatabaseProvider();

        mGhostStorageProvider = factory.buildGhostStorageProvider();

        mExecutor = factory.buildExecutor();

        Logger.log(String.format("Initializing Simperium %s%s", CLIENT_ID, (BuildConfig.DEBUG ? " DEBUG" : "")));
        loadUser();
    }

    public static Simperium getInstance() throws SimperiumNotInitializedException{
    	if(null == simperiumClient)
    		throw new SimperiumNotInitializedException("You must create an instance of Simperium before call this method.");
    	
    	return simperiumClient;
    }

    private void loadUser(){
        user = new User(this);
        mAuthProvider.restoreUser(user);
        if (user.needsAuthorization()) {
            user.setStatus(User.Status.NOT_AUTHORIZED);
        }
    }

    public String getAppId(){
        return appId;
    }

    public User getUser(){
        return user;
    }

    public boolean needsAuthorization(){
        // we don't have an access token yet
        return user.needsAuthorization();
    }

    /**
     * Creates a bucket and starts syncing data and uses the provided
     * Class to instantiate data.
     * 
     * Should only allow one instance of bucketName and the schema should
     * match the existing bucket?
     *
     * @param bucketName the namespace to store the data in simperium
     */
    public <T extends Syncable> Bucket<T> bucket(String bucketName, BucketSchema<T> schema)
    throws BucketNameInvalid {
        return bucket(bucketName, schema, mStorageProvider.createStore(bucketName, schema));
    }

    /**
     * Allow alternate storage mechanisms
     */
    public <T extends Syncable> Bucket<T> bucket(String bucketName, BucketSchema<T> schema, BucketStore<T> storage)
    throws BucketNameInvalid {

        // initialize the bucket
        Bucket<T> bucket = new Bucket<T>(mExecutor, bucketName, schema, user, storage, mGhostStorageProvider);

        // initialize the communication method for the bucket
        Bucket.Channel channel = mChannelProvider.buildChannel(bucket);

        // tell the bucket about the channel
        bucket.setChannel(channel);

        storage.prepare(bucket);
        return bucket;
    }


    /**
     * Creates a bucket and starts syncing data. Users the generic BucketObject for
     * serializing and deserializing data
     *
     * @param bucketName namespace to store the data in simperium
     */
    public Bucket<BucketObject> bucket(String bucketName)
    throws BucketNameInvalid {
        return bucket(bucketName, new BucketObject.Schema(bucketName));
    }

    /**
     * Creates a bucket and uses the Schema remote name as the bucket name.
     */
    public <T extends Syncable> Bucket<T> bucket(BucketSchema<T> schema)
    throws BucketNameInvalid {
        return bucket(schema.getRemoteName(), schema);
    }

    public void setAuthProvider(String providerString){
        mAuthProvider.setAuthProvider(providerString);
    }

    public void setOnUserCreatedListener(OnUserCreatedListener listener){
        onUserCreatedListener = listener;
    }

    protected void notifyOnUserCreatedListener(User user){
        if (onUserCreatedListener != null){
            onUserCreatedListener.onUserCreated(user);
        }
    }

    public User createUser(String email, String password, AuthResponseListener listener){
        user.setCredentials(email, password);
        AuthResponseListener wrapper = new AuthResponseListenerWrapper(listener){
            @Override
            public void onSuccess(User user){
                super.onSuccess(user);
                notifyOnUserCreatedListener(user);
            }
        };
        mAuthProvider.createUser(AuthUtil.makeAuthRequestBody(user), new AuthResponseHandler(user, wrapper));
        return user;
    }

    public User authorizeUser(String email, String password, AuthResponseListener listener){
        user.setCredentials(email, password);
        AuthResponseListener wrapper = new AuthResponseListenerWrapper(listener);
        mAuthProvider.authorizeUser(AuthUtil.makeAuthRequestBody(user), new AuthResponseHandler(user, wrapper));
        return user;
    }

    public void deauthorizeUser(){
        user.setAccessToken(null);
        user.setEmail(null);
        mAuthProvider.deauthorizeUser(user);
        user.setStatus(User.Status.NOT_AUTHORIZED);
    }

    public void onUserStatusChange(User.Status status){
        if (userListener != null) {
            userListener.onUserStatusChange(status);
        }
    }

    public User.StatusChangeListener getUserStatusChangeListener() {
        return userListener;
    }

    public void setUserStatusChangeListener(User.StatusChangeListener listener){
        userListener = listener;
    }

    /**
     * Log message with ChannelProvider.LOG_VERBOSE
     */
    public void log(CharSequence message) {
        mChannelProvider.log(ChannelProvider.LOG_VERBOSE, message);
    }

    /**
     * Log message with provided level see ChannelProvider for log level constants.
     */
    public void log(int level, CharSequence message) {
        mChannelProvider.log(level, message);
    }

    private class AuthResponseListenerWrapper implements AuthResponseListener {

        final private AuthResponseListener mListener;

        public AuthResponseListenerWrapper(final AuthResponseListener listener){
            mListener = listener;
        }

        @Override
        public void onSuccess(User user) {
            mAuthProvider.saveUser(user);
            mListener.onSuccess(user);
        }

        @Override
        public void onFailure(User user, AuthException error) {
            mListener.onFailure(user, error);
        }

    }

    /**
     * Change you database password
     * @param password new password, if it is null, empty password will be used
     * @return true, if password change was successful
     */
    public boolean changePassword(String password){

        if (password == null){
            password = "";
        }
        return mDatabaseProvider.changePassword(password);
    }

    /**
     * Migrate your current not secure database (currently in AndroidClient) to SecureAndriodClient
     * It will keep your current data.
     * @return true, if migration was successful
     * @throws MigrationException
     */
    public boolean migrateToSecure() throws MigrationException{
        boolean migrationSupported = currentFactory instanceof MigrationInterface;
        if (!migrationSupported) {
            throw new MigrationException("Migration is not supported on your current database!");
        } else {
            if (currentFactory instanceof AndroidClient){
                migrateToSqlCipher();
            } else {
                throw new MigrationException("You have already migrated from the default database!");
            }
        }

        return true;
    }

    protected void migrateToSqlCipher() throws MigrationException {

        MigrationInterface current = (MigrationInterface)currentFactory;
        Context migrationContext = current.getMigrationContext();
        SecureAndroidClient client = new SecureAndroidClient(migrationContext);

        boolean successful = client.migrateFromDatabase(current.getDatabaseToMigrate(), MigrationInterface.DatabaseType.DEFAULT);
        if (successful) {
            AndroidClientHelper.setMigrated(migrationContext, true);
        }
    }

}
