package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.aksw.commons.lock.LockUtils;
import org.aksw.commons.store.object.key.api.ObjectResource;
import org.aksw.commons.store.object.key.api.ObjectStore;
import org.aksw.commons.store.object.key.api.ObjectStoreConnection;
import org.aksw.commons.store.object.key.impl.ObjectInfo;
import org.aksw.commons.store.object.key.impl.ObjectStoreImpl;
import org.aksw.commons.store.object.path.api.ObjectSerializer;
import org.aksw.commons.store.object.path.impl.ObjectSerializerKryo;
import org.aksw.commons.txn.api.Txn;
import org.aksw.commons.txn.api.TxnResourceApi;
import org.aksw.commons.txn.impl.TxnHandler;
import org.aksw.commons.txn.impl.TxnMgrImpl;
import org.aksw.commons.util.array.ArrayBuffer;
import org.aksw.commons.util.array.ArrayOps;
import org.aksw.commons.util.ref.RefFuture;
import org.junit.Test;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.collect.TreeRangeSet;

public class SyncPool {
//	protected Timer timer;
//	protected LinkedHashMap<>
//
//	public void put(K key, V value) {
//
//	}

    @Test
    public void test() throws Exception {
        // ObjectStore objectStore = ObjectStoreImpl.create(null, null)
        SliceBufferNew<Object[]> slice = SliceBufferNew.create(ArrayOps.OBJECT, Path.of("/tmp/cache-test"), 128);

        Object[] arr1 = new Object[] {"this", "is", "a", "test"};
        Object[] arr2 = new Object[] {"another", "testarray", "withsome", "moreitems"};

        try (RefFuture<BufferView<Object[]>> refBuffer = slice.getPageForPageId2(0)) {

            LockUtils.runWithLock(slice.getReadWriteLock().writeLock(), () -> {
                BufferView<Object[]> buffer = refBuffer.await();
                try {
                    buffer.getRangeBuffer().putAll(0, arr1, 0, 4);
                    buffer.getRangeBuffer().putAll(8, arr2, 0, 4);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        slice.sync();

    }

//
//	public static void mainZ(String[] args) throws Exception {
//
//		KryoPool kryoPool = SmartRangeCacheImpl.createKyroPool(null);
//		ObjectSerializer objectSerializer = ObjectSerializerKryo.create(kryoPool);
//
//		Path path = Path.of("/tmp/object-store");
//
//		ObjectStore objStore = ObjectStoreImpl.create(path, objectSerializer);
//
//		try (RefFuture<ObjectInfo> ref = objStore.claim("test", "foo.bar")) {
//			System.out.println(ref.await());
//		}
//
//
//
//		try (ObjectStoreConnection conn = objStore.getConnection()) {
//
//			conn.begin(true);
//
//			try (ObjectResource res = conn.access("test", "foo.bar")) {
//
//				// Loads the content into memory and prevents spill to disk
//				// res.claimContent();
//
//				Object obj = res.getContent();
//				System.out.println("Read content: " + obj);
//
//				res.setContent("hello world2");
//
//				// Save any new content to disk. Does not commit the change.
//				res.save();
//			}
//
//			// Ref<ObjectResourec> ref = res.claim();
//
//
//
//			conn.commit();
//
//		}
//
//
//		System.out.println("done");
//
//	}


    public static void rwlTest() {
        ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
        rrwl.writeLock().lock();
        rrwl.writeLock().lock();
        rrwl.writeLock().lock();
        rrwl.writeLock().unlock();
        System.out.println(rrwl.isWriteLockedByCurrentThread());
        rrwl.writeLock().unlock();
        System.out.println(rrwl.isWriteLockedByCurrentThread());
        rrwl.writeLock().unlock();
        System.out.println(rrwl.isWriteLockedByCurrentThread());
    }

    public static void mainY(String[] args) throws Exception {
        RangeBufferImpl<Object[]> base = new RangeBufferImpl<>(TreeRangeSet.create(), 0, ArrayBuffer.create(ArrayOps.OBJECT, 10));

        base.putAll(3, new String[] { "this", "is", "a","test"}, 0, 4);

        RangeBufferImpl<Object[]> overlay = new RangeBufferImpl<>(TreeRangeSet.create(), 0, ArrayBuffer.create(ArrayOps.OBJECT, 10));

        RangeBufferUnion<Object[]> union = RangeBufferUnion.create(overlay, base);

        Object[] buf = new Object[10];

        union.readInto(buf, 0, 4, 2);


        System.out.println(Arrays.toString(buf));

    }


    public static void mainX(String[] args) throws Exception {
        Path root = Path.of("/tmp/cache-test");
        Files.createDirectories(root);

        TxnMgrImpl txnMgr = TxnMgrImpl.createSimple(root);
        TxnHandler txnHandler = new TxnHandler(txnMgr);
        txnHandler.cleanupStaleTxns();

        Txn txn = txnMgr.newTxn(true, true);
        {
            TxnResourceApi res = txn.getResourceApi("file1.txt");
            res.declareAccess();
            res.lock(true);
            res.getFileSync().putContent(out -> {
                PrintStream ps = new PrintStream(out);
                ps.println("Hello file1x");
                ps.flush();
            });
        }

        {
            TxnResourceApi res = txn.getResourceApi("file2.txt");
            res.declareAccess();
            res.lock(true);
            res.getFileSync().putContent(out -> {
                PrintStream ps = new PrintStream(out);
                ps.println("Hello file2x");
                ps.flush();
            });
        }
        // txn.addCommit();

        txnHandler.abort(txn);
        // txnHandler.commit(txn);

        // txn.listVisibleFiles(null)
    }


}
