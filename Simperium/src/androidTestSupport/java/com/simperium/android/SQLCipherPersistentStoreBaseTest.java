package com.simperium.android;

import net.sqlcipher.database.SQLiteDatabase;
import android.test.ActivityInstrumentationTestCase2;

import com.simperium.client.Bucket;
import com.simperium.client.BucketSchema;
import com.simperium.client.GhostStorageProvider;
import com.simperium.client.User;

import com.simperium.database.DatabaseProvider;
import com.simperium.database.SQLiteDatabaseWrapper;

import com.simperium.storage.StorageProvider.BucketStore;
import com.simperium.models.Note;

import com.simperium.test.MockChannel;
import com.simperium.test.MockGhostStore;
import com.simperium.test.MockExecutor;

import com.simperium.database.SQLCipherDatabaseWrapper;
import com.simperium.database.SQLCipherDatabaseWrapperHelper;
import com.simperium.database.encryption.EncryptionLogic;
import com.simperium.database.encryption.EncryptionException;

import static com.simperium.TestHelpers.makeUser;

public abstract class SQLCipherPersistentStoreBaseTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    public static final String MASTER_TABLE = "sqlite_master";
    public static final String BUCKET_NAME="bucket";

    protected LoginActivity mActivity;
    
    protected PersistentStore mStore;
    protected BucketStore<Note> mNoteStore;
    protected SQLiteDatabase mDatabase;
    protected String mDatabaseName = "simperium-test-data";
    protected String[] mTableNames = new String[]{"indexes", "objects", "value_caches"};
    protected Bucket<Note> mBucket;
    protected User mUser;
    protected BucketSchema mSchema;
    protected GhostStorageProvider mGhostStore;
    protected SQLCipherDatabaseWrapperHelper mDbHelper;

    protected EncryptionLogic encrypterLogic  = new EncryptionLogic() {
        @Override
        public String getDatabaseEncryptionKey() {
            return "pwd";
        }

        @Override
        public String applyEncryption(String password) throws EncryptionException {
            return password;
        }

        @Override
        public boolean saveDatabaseEncryptionKey(String key) {
            return false;
        }
    };

    public SQLCipherPersistentStoreBaseTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp()
    throws Exception {

        super.setUp();

        setActivityInitialTouchMode(false);
        mUser = makeUser();
        mActivity = getActivity();
        SQLiteDatabase.loadLibs(mActivity);
        mDbHelper = new SQLCipherDatabaseWrapperHelper(mActivity, mDatabaseName, encrypterLogic);
        mDatabase = mDbHelper.getDatabase();
        mGhostStore = new MockGhostStore();
        mStore = new PersistentStore(new DatabaseProvider(mDbHelper));
        mSchema = new Note.Schema();
        mNoteStore = mStore.createStore(BUCKET_NAME, mSchema);
        mBucket = new Bucket<Note>(MockExecutor.immediate(), BUCKET_NAME, mSchema, mUser, mNoteStore, mGhostStore);
        Bucket.Channel channel = new MockChannel(mBucket);
        mBucket.setChannel(channel);
        mNoteStore.prepare(mBucket);
    }

    @Override
    protected void tearDown() throws Exception {
        mActivity.deleteDatabase(mDatabaseName);
        super.tearDown();
    }

}