package com.simperium;

import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.test.ActivityInstrumentationTestCase2;

import com.simperium.android.LoginActivity;
import com.simperium.android.QueueSerializer;
import com.simperium.database.DatabaseProvider;
import com.simperium.database.SQLCipherDatabaseWrapper;
import com.simperium.database.SQLCipherDatabaseWrapperHelper;
import com.simperium.database.encryption.EncryptionLogic;
import com.simperium.database.encryption.EncryptionException;
import com.simperium.client.Bucket;
import com.simperium.client.BucketObject;
import com.simperium.client.Change;
import com.simperium.client.Channel.SerializedQueue;
import com.simperium.test.MockBucket;

public class SQLCipherQueueSerializerTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    protected QueueSerializer mSerializer;
    protected SQLiteDatabase mDatabase;

    protected Bucket<BucketObject> mBucket;

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

    public SQLCipherQueueSerializerTest() {
        super(LoginActivity.class);
    }

    protected void setUp() throws Exception {
        SQLiteDatabase.loadLibs(getActivity());
        SQLCipherDatabaseWrapperHelper dbHelper = new SQLCipherDatabaseWrapperHelper(getActivity(), "queue-test-enc", encrypterLogic);
        mDatabase = dbHelper.getDatabase();
        mSerializer = new QueueSerializer(new DatabaseProvider(dbHelper));
        BucketObject.Schema schema = new BucketObject.Schema("mock-bucket-enc");
         mBucket = MockBucket.buildBucket(schema);
    }

    public void testDatabaseSetup() throws Exception {
        assertTableExists(mDatabase, QueueSerializer.TABLE_NAME);
    }

    public void testQueueChange() throws Exception {
        BucketObject object = mBucket.newObject();
        object.setProperty("title", "Hola Mundo");

        Change change = new Change(Change.OPERATION_MODIFY, mBucket.getName(), object.getSimperiumKey());

        mSerializer.onQueueChange(change);

        SerializedQueue queue = mSerializer.restore(mBucket);
        assertEquals(1, queue.queued.size());

        mSerializer.onDequeueChange(change);

        queue = mSerializer.restore(mBucket);
        assertEquals(0, queue.queued.size());
    }

    public void testUpdateChange() throws Exception {
        BucketObject object = mBucket.newObject();
        object.setProperty("title", "Hola Mundo");

        Change change = new Change(Change.OPERATION_REMOVE, mBucket.getName(), object.getSimperiumKey());
        mSerializer.onQueueChange(change);

        SerializedQueue queue = mSerializer.restore(mBucket);
        assertEquals(1, queue.queued.size());

        mSerializer.onSendChange(change);

        queue = mSerializer.restore(mBucket);
        assertEquals(1, queue.pending.size());
        assertEquals(0, queue.queued.size());

    }

    public static void assertTableExists(SQLiteDatabase database, String tableName) {
        Cursor cursor = database.query("sqlite_master", new String[]{"name"}, "type=? AND name=?", new String[]{"table", tableName}, "name", null, null, null);
        assertEquals(String.format("Table %s does not exist in %s", tableName, database), 1, cursor.getCount());
    }

}
