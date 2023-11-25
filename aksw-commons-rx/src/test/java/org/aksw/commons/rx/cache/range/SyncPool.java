package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.buffer.plain.BufferOverArray;
import org.aksw.commons.path.core.PathOpsStr;
import org.aksw.commons.rx.lookup.ListPaginatorFromList;
import org.aksw.commons.txn.api.Txn;
import org.aksw.commons.txn.api.TxnResourceApi;
import org.aksw.commons.txn.impl.TxnHandlerImpl;
import org.aksw.commons.txn.impl.TxnMgrImpl;
import org.aksw.commons.util.lock.LockUtils;
import org.junit.Test;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;

//public class SyncPool {
////	protected Timer timer;
////	protected LinkedHashMap<>
////
////	public void put(K key, V value) {
////
////	}
//
//
//	@Test
//	public void testRangeCache() throws IOException {
//		ArrayOps<Object[]> arrayOps = ArrayOps.OBJECT;
//
//        SliceBufferNew<Object[]> slice = SliceBufferNew.create(arrayOps, Path.of("/tmp/cache-test"), PathOpsStr.create("test1"), 128);
//
//        SequentialReaderSource<Object[]> source = SequentialReaderSourceRx.create(arrayOps, ListPaginatorFromList.wrap(
//        		IntStream.range('a', 'z').mapToObj(i -> "item " + (char)i).collect(Collectors.toList())));
//
//
//        AdvancedRangeCacheNew<Object[]> cache = new AdvancedRangeCacheNew<>(source, slice, Duration.ofSeconds(5), 10000, Duration.ofSeconds(10));
//
//        Range<Long> requestRange = Range.atLeast(10l);
//        SequentialReader<Object[]> baseReader = source.newInputStream(requestRange);
//
//        Object[] buffer = arrayOps.create(10);
//        // baseReader.read(buffer, 0, buffer.length);
//
//        System.out.println(Arrays.toString(buffer));
//
//
//        SequentialReader<Object[]> cachedReader = cache.newInputStream(requestRange);
//
//        int n = 0;
//        int c = 0;
//        while ((c = cachedReader.read(buffer, n, buffer.length - n)) > 0) {
//        	n += c;
//        }
//
//        slice.sync();
//        System.out.println(Arrays.toString(buffer));
//
////        SmartRangeCacheNew<Object[]> cache = new SmartRangeCacheNew<>();
//
////        return SmartRangeCacheImpl.wrap(
////                backend, SmartRangeCacheImpl.createKeyObjectStore(
////                        Paths.get("/tmp/test/" + testId),
////                        SmartRangeCacheImpl.createKyroPool(null)), 1024, 10, Duration.ofSeconds(1), 10000, 1000);
//
//
//	}
//
//    // @Test
//    public void test() throws Exception {
//        // ObjectStore objectStore = ObjectStoreImpl.create(null, null)
//        SliceBufferNew<Object[]> slice = SliceBufferNew.create(ArrayOps.OBJECT, Path.of("/tmp/cache-test"), PathOpsStr.create("/test1"), 128);
//
//        Object[] arr1 = new Object[] {"this", "is", "a", "test"};
//        Object[] arr2 = new Object[] {"another", "testarray", "withsome", "moreitems"};
//
//
//        // try (RefFuture<BufferView<Object[]>> refBuffer = slice.getPageForPageId2(0)) {
//        try (SliceAccessor<Object[]> accessor = slice.newSliceAccessor()) {
//            LockUtils.runWithLock(slice.getReadWriteLock().writeLock(), () -> {
//
//                accessor.claimByOffsetRange(0, 2000);
//
//                accessor.lock();
//
//                // BufferView<Object[]> buffer = refBuffer.await();
//                try {
//                    accessor.write(0, arr1, 0, 4);
//                    accessor.write(8, arr2, 0, 4);
//                    accessor.write(130, arr2, 0, 4);
//                    accessor.write(1300, arr1, 0, 4);
////                    buffer.getRangeBuffer().putAll(0, arr1, 0, 4);
////                    buffer.getRangeBuffer().putAll(8, arr2, 0, 4);
////                    buffer.getRangeBuffer().putAll(130, arr2, 0, 4);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    accessor.unlock();
//                }
//            });
//        }
//
//        slice.sync();
//
//    }
//
////
////	public static void mainZ(String[] args) throws Exception {
////
////		KryoPool kryoPool = SmartRangeCacheImpl.createKyroPool(null);
////		ObjectSerializer objectSerializer = ObjectSerializerKryo.create(kryoPool);
////
////		Path path = Path.of("/tmp/object-store");
////
////		ObjectStore objStore = ObjectStoreImpl.create(path, objectSerializer);
////
////		try (RefFuture<ObjectInfo> ref = objStore.claim("test", "foo.bar")) {
////			System.out.println(ref.await());
////		}
////
////
////
////		try (ObjectStoreConnection conn = objStore.getConnection()) {
////
////			conn.begin(true);
////
////			try (ObjectResource res = conn.access("test", "foo.bar")) {
////
////				// Loads the content into memory and prevents spill to disk
////				// res.claimContent();
////
////				Object obj = res.getContent();
////				System.out.println("Read content: " + obj);
////
////				res.setContent("hello world2");
////
////				// Save any new content to disk. Does not commit the change.
////				res.save();
////			}
////
////			// Ref<ObjectResourec> ref = res.claim();
////
////
////
////			conn.commit();
////
////		}
////
////
////		System.out.println("done");
////
////	}
//
//
//    public static void rwlTest() {
//        ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
//        rrwl.writeLock().lock();
//        rrwl.writeLock().lock();
//        rrwl.writeLock().lock();
//        rrwl.writeLock().unlock();
//        System.out.println(rrwl.isWriteLockedByCurrentThread());
//        rrwl.writeLock().unlock();
//        System.out.println(rrwl.isWriteLockedByCurrentThread());
//        rrwl.writeLock().unlock();
//        System.out.println(rrwl.isWriteLockedByCurrentThread());
//    }
//
//    public static void mainY(String[] args) throws Exception {
//        RangeBufferImpl<Object[]> base = new RangeBufferImpl<>(TreeRangeSet.create(), 0, ArrayBuffer.create(ArrayOps.OBJECT, 10));
//
//        base.write(3, new String[] { "this", "is", "a","test"}, 0, 4);
//
//        RangeBufferImpl<Object[]> overlay = new RangeBufferImpl<>(TreeRangeSet.create(), 0, ArrayBuffer.create(ArrayOps.OBJECT, 10));
//
//        RangeBufferUnion<Object[]> union = RangeBufferUnion.create(overlay, base);
//
//        Object[] buf = new Object[10];
//
//        union.readInto(buf, 0, 4, 2);
//
//
//        System.out.println(Arrays.toString(buf));
//
//    }
//
//
//    public static void mainX(String[] args) throws Exception {
//        Path root = Path.of("/tmp/cache-test");
//        Files.createDirectories(root);
//
//        TxnMgrImpl txnMgr = TxnMgrImpl.createSimple(root);
//        TxnHandler txnHandler = new TxnHandler(txnMgr);
//        txnHandler.cleanupStaleTxns();
//
//        Txn txn = txnMgr.newTxn(true, true);
//        {
//            TxnResourceApi res = txn.getResourceApi(PathOpsStr.create("file1.txt"));
//            res.declareAccess();
//            res.lock(true);
//            res.getFileSync().putContent(out -> {
//                PrintStream ps = new PrintStream(out);
//                ps.println("Hello file1x");
//                ps.flush();
//            });
//        }
//
//        {
//            TxnResourceApi res = txn.getResourceApi(PathOpsStr.create("file2.txt"));
//            res.declareAccess();
//            res.lock(true);
//            res.getFileSync().putContent(out -> {
//                PrintStream ps = new PrintStream(out);
//                ps.println("Hello file2x");
//                ps.flush();
//            });
//        }
//        // txn.addCommit();
//
//        txnHandler.abort(txn);
//        // txnHandler.commit(txn);
//
//        // txn.listVisibleFiles(null)
//    }
//
//
//}
