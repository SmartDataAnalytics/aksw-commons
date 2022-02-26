package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.LongFunction;

import org.aksw.commons.cache.async.AsyncClaimingCache;
import org.aksw.commons.cache.async.AsyncClaimingCacheImpl;
import org.aksw.commons.io.util.Sync;
import org.aksw.commons.lock.LockUtils;
import org.aksw.commons.store.object.key.api.ObjectResource;
import org.aksw.commons.store.object.key.api.ObjectStore;
import org.aksw.commons.store.object.key.api.ObjectStoreConnection;
import org.aksw.commons.store.object.key.impl.ObjectStoreImpl;
import org.aksw.commons.store.object.path.api.ObjectSerializer;
import org.aksw.commons.store.object.path.impl.ObjectSerializerKryo;
import org.aksw.commons.txn.api.TxnApi;
import org.aksw.commons.txn.impl.PathDiffState;
import org.aksw.commons.txn.impl.PathState;
import org.aksw.commons.util.array.ArrayBuffer;
import org.aksw.commons.util.array.ArrayOps;
import org.aksw.commons.util.array.Buffer;
import org.aksw.commons.util.ref.RefFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;
import com.google.common.util.concurrent.MoreExecutors;



// The outside only sees a buffer - but internally it has a structure that enables serializing the changed regions

public class SliceBufferNew<A>
    implements SliceWithPages<A>, Sync
    // implements SliceWithAutoSync<T>
{
    protected Logger logger = LoggerFactory.getLogger(SliceBufferNew.class);


    // Storage layer for transactional saving of buffers
    protected ObjectStore objectStore;

    protected org.aksw.commons.path.core.Path<String> objectStoreBasePath;

    // Array abstraction; avoids having mainly used to abstract from byte[] and Object[] and consequently having to build
    // separate cache implementations
    protected ArrayOps<A> arrayOps;


    protected AsyncClaimingCache<Long, BufferView<A>> pageCache;


    // Sync properties: sync unclaimed pages / snapshot; in either case sync delay; sync if too many changes
//	public static <A> SliceBufferNew<A> getOrCreate(Path folder, ArrayOps<A> arrayOps, int pageSize) {
//		ObjectStore objectStore = ObjectStoreImpl.create(folder, ObjectSerializerKryo.create(SmartRangeCacheImpl.createKyroPool(null)));
//	}

    protected int pageSize;


    // A read/write lock for synchronizing reads/writes to the slice
    protected ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    // A condition that is signalled whenever content or metadata changes
    protected Condition hasDataCondition = readWriteLock.writeLock().newCondition();

    // protected SparseVersionedBuffer<T> changes;

    protected RangeSetDelegateMutable<Long> baseRanges;

    // The baseMetaData is backed by baseRanges - baseRanges's delegate may be modified any time
    protected SliceMetaData baseMetaData;
    protected PathDiffState baseMetaDataStatus;

    // protected RangeSet<Long> liveRangeChanges;

    protected SliceMetaData liveMetaData;
    //protected AsyncClaimingCache<Long, A> loadedPages;

    protected SliceMetaData syncMetaData;
    protected PathState metaDataIdentity; // When the metadata was loaded

    protected Duration syncDelay;
    protected volatile ScheduledFuture<?> syncFuture = null;

    /**
     * Attribute for advertising detection of external changes to the base data.
     * Modification demands the global write lock. Whenever a change to the
     * base data is detected (during sync) then the generation is incremented. All clients should then
     * re-schedule their read/writes. */
    protected int liveGeneration = 0;

    // The buffer with all active in-memory changes
    protected RangeBufferDelegateMutable<A> liveChanges = new RangeBufferDelegateMutableImpl<>();


//	@Override
//	public void write(long offsetInBuffer, A arrayWithItemsOfTypeT, int arrOffset, int arrLength) throws IOException {
//		super.write(offsetInBuffer, arrayWithItemsOfTypeT, arrOffset, arrLength);
//		scheduleSync();
//	};


    protected ScheduledExecutorService syncScheduler = MoreExecutors.getExitingScheduledExecutorService(new ScheduledThreadPoolExecutor(1));

    protected void scheduleSync() {
        if (syncFuture == null || syncFuture.isDone()) {
            // syncFuture = syncScheduler.schedule(() -> { sync(); return null; }, syncDelay.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    protected RangeBufferDelegateMutable<A> syncChanges = new RangeBufferDelegateMutableImpl<>();

    protected LongFunction<String> pageIdToFileName;


    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    public void setMinimumKnownSize(long minimumKnownSize) {
        this.liveMetaData.setMinimumKnownSize(minimumKnownSize);
    }

    public void setMaximumKnownSize(long maximumKnownSize) {
        this.liveMetaData.setMaximumKnownSize(maximumKnownSize);
    }


    public SliceBufferNew(
            ArrayOps<A> arrayOps,
            ObjectStore objectStore,
            org.aksw.commons.path.core.Path<String> objectStoreBasePath,
            int pageSize,
            Duration syncDelay) {
        super();
        this.arrayOps = arrayOps;
        this.objectStore = objectStore;
        this.objectStoreBasePath = objectStoreBasePath;
        this.syncDelay = syncDelay;
        // this.pageSize = pageSize;

        // pageSize is only available after metadata is loaded
        loadMetaData(pageSize);

        this.syncChanges.setDelegate(newChangeBuffer());
    }

    public void loadMetaData(int pageSize) {
        baseRanges = new RangeSetDelegateMutableImpl<>();

        try (ObjectStoreConnection conn = objectStore.getConnection()) {
            TxnApi.execRead(conn, () -> {
                lockAndSyncMetaData(conn, pageSize);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.pageSize = baseMetaData.getPageSize();

        this.liveChanges.setDelegate(newChangeBuffer());
        this.liveMetaData = copyWithNewRanges(baseMetaData, RangeSetOps.union(liveChanges.getRanges(), baseRanges));


        DecimalFormat df = new DecimalFormat();
        df.setMinimumIntegerDigits(8);
        df.setGroupingUsed(false);

        this.pageIdToFileName = pageId -> "segment-" + df.format(pageId) + ".ser";

        this.pageCache = AsyncClaimingCacheImpl
                .<Long, BufferView<A>>newBuilder(Caffeine.newBuilder())
                .setCacheLoader(this::loadPage)
                .build();
                ;
    }


    public static <A> SliceBufferNew<A> create(ArrayOps<A> arrayOps, ObjectStore objectStore, org.aksw.commons.path.core.Path<String> objectStoreBasePath, int pageSize, Duration syncDelay) {
        return new SliceBufferNew<>(arrayOps, objectStore, objectStoreBasePath, pageSize, syncDelay);
    }

    public static <A> SliceBufferNew<A> create(ArrayOps<A> arrayOps, Path repoPath, org.aksw.commons.path.core.Path<String> objectStoreBasePath, int pageSize, Duration syncDelay) {
        KryoPool kryoPool = SmartRangeCacheImpl.createKyroPool(null);
        ObjectSerializer objectSerializer = ObjectSerializerKryo.create(kryoPool);
        ObjectStore objectStore = ObjectStoreImpl.create(repoPath, objectSerializer);

        return new SliceBufferNew<>(arrayOps, objectStore, objectStoreBasePath, pageSize, syncDelay);
    }


    protected RangeBuffer<A> newChangeBuffer() {
        Buffer<A> actualBuffer = PagedBuffer.create(arrayOps, pageSize);


        // This is not the best place for scheduling sync because
        // we can't capture changes to the known min/max sizes here

        // Wrap the buffer such than modifications schedule a sync action
//        return RangeBufferImpl.create(
//                new BufferDelegateBase<A>() {
//                    @Override
//                    public Buffer<A> getDelegate() {
//                        return actualBuffer;
//                    }
//
//                    @Override
//                    public void write(long offsetInBuffer, A arrayWithItemsOfTypeT, int arrOffset, int arrLength)
//                            throws IOException {
//                        super.write(offsetInBuffer, arrayWithItemsOfTypeT, arrOffset, arrLength);
//                        scheduleSync();
//                    }
//                });
        //return RangeBufferImpl.create(rangeSet, 0, PagedBuffer.create(arrayOps, pageSize));
        return RangeBufferImpl.create(actualBuffer);
    }



    // protected int pageSize;
    // protected AsyncClaimingCache<Long, BufferWithGenerationImpl<T>> pageCache;
    //protected AsyncClaimingCache<String, SliceMetaData> metadataCache;
    // protected Map<Long, Object[]>


    public SliceAccessor<A> newSliceAccessor() {
        return new SliceAccessorImpl<>(this);
    }


    public RefFuture<BufferView<A>> getPageForPageId(long pageId) {
        return pageCache.claim(pageId);
    }

    @Override
    public RangeSet<Long> getGaps(Range<Long> requestRange) {
        return liveMetaData.getGaps(requestRange);
    }


    public boolean hasMetaDataChanged() {
        String resourceName = "metadata.ser";
        PathDiffState recencyStatus = objectStore.fetchRecencyStatus(objectStoreBasePath.resolve(resourceName));
        return !recencyStatus.equals(baseMetaDataStatus);
    }


    @Override
    public void checkForUpdate() {

    }

    /** Returns the metadata generation */
    public int lockAndSyncMetaData(ObjectStoreConnection conn, int fallbackPageSize) {

        boolean hasMetaDataChanged = hasMetaDataChanged();

        String resourceName = "metadata.ser";
        ObjectResource res = conn.access(objectStoreBasePath.resolve(resourceName));

        int result;

        if (hasMetaDataChanged) {
            logger.info("Metadata was externally modified on disk... attempting to acquire lock for reload...");

            // Re-check recency status now that the resource is locked
            PathDiffState status = res.fetchRecencyStatus();

            SliceMetaData newMetaData = (SliceMetaData)res.loadNewInstance();
            // Objects.requireNonNull(newMetaData, "Deserialization of metadata yeld null");

            // System.out.println("Acquired readWrite lock at: " + StackTraceUtils.toString(Thread.currentThread().getStackTrace()));


            result = LockUtils.runWithLock(readWriteLock.writeLock(), () -> {
                logger.info("Lock for reload acquired");
                if (newMetaData != null) {
                    logger.info("Loaded metadata: " + newMetaData);
                    baseMetaData = newMetaData;
                    baseMetaDataStatus = status;
                } else {
                    logger.info("Created fresh slice metadata");
                    baseMetaData = new SliceMetaDataImpl(fallbackPageSize);
                    baseMetaDataStatus = null; // TODO Use a non-null placeholder value
                }
                baseRanges.setDelegate(baseMetaData.getLoadedRanges());

                ++liveGeneration;
                return liveGeneration;
            });
        } else {
            result = LockUtils.runWithLock(readWriteLock.readLock(), () -> liveGeneration);
        }

        return result;
    }

    public BufferView<A> loadPage(long pageId) {

        String fileName = pageIdToFileName.apply(pageId);

        BufferWithAutoReloadOnAccess baseBuffer = new BufferWithAutoReloadOnAccess(fileName);

        baseBuffer.reloadIfNeeded().whenComplete((b, t) -> {
            if (t != null) {
                logger.error("Reloading buffer failed", t);
            }
        });

        long pageOffset = PageUtils.getPageOffsetForId(pageId, pageSize);

        // baseMetaData.getLoadedRanges()
        RangeBuffer<A> baseRangeBuffer = RangeBufferImpl.create(baseRanges, pageOffset, baseBuffer);

        // RangeBuffer<A> baseBuffer = RangeBufferImpl.create(baseRanges, pageOffset, buffer);
        RangeBuffer<A> deltaRangeBuffer1 = syncChanges.slice(pageOffset, pageSize);
        RangeBuffer<A> deltaRangeBuffer2 = liveChanges.slice(pageOffset, pageSize);

        RangeBuffer<A> unionBuffer = RangeBufferUnion.create(baseRangeBuffer, deltaRangeBuffer1);
        RangeBuffer<A> finalUnionBuffer = RangeBufferUnion.create(deltaRangeBuffer2, unionBuffer);


        // Wrap the buffer such that the minimum known size is updated?

        return new InternalBufferView(baseBuffer, finalUnionBuffer);
    }

    /** Only call this method after acquiring the write lock! */
    public SliceWithAutoSync<A> updateMinimumKnownSize(long size) {
        this.liveMetaData.updateMinimumKnownSize(size);
        return this;
    }

    /** Only call this method after acquiring the write lock! */
    public SliceWithAutoSync<A> updateMaximumKnownSize(long size) {
        this.liveMetaData.updateMaximumKnownSize(size);
        return this;
    }

    /** Loading a page requires locking the metadata file - by loading pages in batch we can reduce redundant locking and reduce the risk of in-between external changes */
    public BufferView<A> loadPages(boolean isEager, Set<Long> pageIds) {
        // The crucial point is that with eager loading we want to lock the metadata now
        // and unlock it once all the requested pages are loaded

        throw new UnsupportedOperationException();
    }



    public ArrayOps<A> getArrayOps() {
        return arrayOps;
    }


    /**
     * Create an immutable snapshot of the current state of the slice suitable for syncing to disk.
     * This method only requires brief locking in order to set up a new layer of change tracking on top
     * of the existing one.
     * @throws IOException
     *
     */
//    public Slice<A> newSnapshot() {
//    	// Create a new buffer for tracking changes and ...
//    	RangeBuffer<A> newDelta = newChangeBuffer();
//
//    	// ... union it with the old one - the old one is now considered immutable
//    	RangeBuffer<A> unionDelta = RangeBufferUnion.create(newDelta, changes);
//
//    	// Acquire the write lock - usually we only need it briefly to update the pointer
//    	LockUtils.runWithLock(readWriteLock.writeLock(), () -> {
//    		changes = unionDelta;
//    	});
//
//
//    	sync();
//    }


    public static SliceMetaData copyWithNewRanges(SliceMetaData base, RangeSet<Long> rangeSet) {
        return new SliceMetaDataImpl(
                base.getPageSize(),
                rangeSet,
                base.getFailedRanges(),
                base.getMinimumKnownSize(),
                base.getMaximumKnownSize()
        );
    }

    @Override
    public void sync() throws IOException {
        // The somewhat complex part is now to compute the new metadata rangeset and making sure that
        // existing data on disk is still consistently described.
        // This means we need to ensure our in-memory copy of the metadata in up-to-date and then 'patch in'
        // only the ranges of modified pages

        Stopwatch stopwatch = Stopwatch.createStarted();

        // Before this method returns the sync changes are are merged into the base buffer
        LockUtils.runWithLock(readWriteLock.writeLock(), () -> {
            syncChanges.setDelegate(liveChanges.getDelegate());
            liveChanges.setDelegate(newChangeBuffer());
            // liveRangeChanges = liveChanges.getRanges();

            syncMetaData = liveMetaData;

            RangeSet<Long> rs = RangeSetUnion.create(liveChanges.getRanges(), syncMetaData.getLoadedRanges());

            liveMetaData = new SliceMetaDataImpl(
                    syncMetaData.getPageSize(),
                    rs,
                    TreeRangeMap.create(),
                    syncMetaData.getMinimumKnownSize(),
                    syncMetaData.getMaximumKnownSize()
            );
        });

        // Incremented on detection of external changes
        int nextGeneration = liveGeneration;

        try (ObjectStoreConnection conn = objectStore.getConnection()) {
            conn.begin(true);

            ObjectResource res = conn.access(objectStoreBasePath.resolve("metadata.ser"));

            int generationNow = lockAndSyncMetaData(conn, pageSize);

//            // PathDiffState cachedStatus = res.getRecencyStatus();
//
            // PathDiffState recentStatus = res.fetchRecencyStatus();
//
//            ObjectInfo oi = null;
//            if (!recentStatus.equals(baseMetaDataStatus)) {
//                oi = res.loadNewInstance();
//            }
//            lockAndSyncMetaData(conn, pageSize);


//            // Cross check the metadata whether reloading is needed
//            boolean isReloaded = false;
//
//            // If reloaded then create the diff
//            if (isReloaded) {
//                SliceMetaData reloaded = oi.getObject();
//
//                Set<Range<Long>> diff = RangeSetUtils.symmetricDifference(syncMetaData.getLoadedRanges(), reloaded.getLoadedRanges());
//
//                Set<Long> externallyModifiedPages = PageUtils.touchedPageIndices(diff, pageSize);
//
//            }

            TreeRangeSet<Long> materializedRanges = TreeRangeSet.create();
            materializedRanges.addAll(baseRanges);
            materializedRanges.addAll(syncChanges.getRanges());

            SliceMetaData newBaseMetadata = copyWithNewRanges(syncMetaData, materializedRanges);

            // res.setContent(newBaseMetadata);

            res.save(newBaseMetadata);

            // If the metadata changed on disk we need to reload it and check which pages need to be reloaded as well
            // FIXME Update idsOfDirtyPages with changes from modified metadata

            // Check whether the timestamp (generation) of the in memory copy of the meta data matches that on disk


            Set<Long> idsOfDirtyPages = PageUtils.touchedPageIndices(syncChanges.getRanges().asRanges(), pageSize);

            for (long pageId : idsOfDirtyPages) {
                 String pageFileName = pageIdToFileName.apply(pageId);

                // ObjectResource pageRes = conn.access(pageFileName);

                // RangeBufferBuffer<A> buffer = loadedPages.claim(pageId).await();
                // try (RefFuture<ObjectInfo> pageBuffer = objectStore.claim(pageFileName)) {
                try (RefFuture<BufferView<A>> pageBuffer = pageCache.claim(pageId)) {
                    long offset = PageUtils.getPageOffsetForId(pageId, pageSize);

//                    A baseArray = pageBuffer.await().getObject();
//                    if (baseArray == null) {
//                        baseArray = arrayOps.create(pageSize);
//                    }
                    InternalBufferView bufferView = (InternalBufferView) pageBuffer.await();
                    BufferWithAutoReloadOnAccess baseBuffer = bufferView.getBaseBuffer();

                    // Synchronous update of the page (we could trigger async update on all first and then wait for all here)
                    baseBuffer.updateIfNeeded(generationNow, conn);

                    RangeBuffer<A> baseRangeBuffer = RangeBufferImpl.create(baseRanges, offset, baseBuffer);
                    RangeBuffer<A> subRangeBuffer = syncChanges.slice(offset, pageSize);
                    RangeBuffer<A> unionRangeBuffer = RangeBufferUnion.create(subRangeBuffer, baseRangeBuffer);

                    A array = arrayOps.create(pageSize);

                    // We need to wrap the array as a range buffer
                    RangeBuffer<A> arrayWrapper = RangeBufferImpl.create(ArrayBuffer.create(arrayOps, array));
                    unionRangeBuffer.transferTo(0, arrayWrapper, 0, pageSize);

                    // pageRes.setContent(array);
                    ObjectResource pageRes = conn.access(objectStoreBasePath.resolve(pageFileName));
                    pageRes.save(array);
                }

            }


            conn.commit();


            // Update buffer and metadata to the materialized data
            LockUtils.runWithLock(readWriteLock.writeLock(), () -> {
                // syncMetaData.getLoadedRanges().clear();
                baseRanges.setDelegate(materializedRanges);
                syncChanges.setDelegate(newChangeBuffer());

//        		baseMetaData = newBaseMetadata;
//
//
//        		// Remove the union with the syncChanges which we injected when entering the sync() method from live metadata
//        		RangeSet<Long> rs = RangeSetUnion.create(liveChanges.getRanges(), syncMetaData.getLoadedRanges());
//        		liveMetaData = copyWithNewRanges(liveMetaData, materializedMetaData)
//        		liveMetaData = new SliceMetaDataImpl(
//        				baseMetaData.getPageSize(),
//        				liveChanges,
//        				TreeRangeMap.create(),
//        				syncMetaData.getMinimumKnownSize(),
//        				syncMetaData.getMaximumKnownSize()
//        		);
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        logger.info("Synchronization completed in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000.0f + " seconds");
    }


    @Override
    public RangeSet<Long> getLoadedRanges() {
        return liveMetaData.getLoadedRanges();
    }

    @Override
    public long getMinimumKnownSize() {
        return liveMetaData.getMinimumKnownSize();
    }

    @Override
    public long getMaximumKnownSize() {
        return liveMetaData.getMaximumKnownSize();
    }

    @Override
    public Condition getHasDataCondition() {
        return hasDataCondition;
    }

//    @Override
//    public RefFuture getMetaData() {
//        throw new UnsupportedOperationException();
//    }

//    @Override
//    public Iterator blockingIterator(long offset) {
//        throw new UnsupportedOperationException();
//    }


    @Override
    public long getPageSize() {
        return pageSize;
    }


    class InternalBufferView
        implements BufferView<A> {

        protected BufferWithAutoReloadOnAccess baseBuffer;
        protected RangeBuffer<A> rangeBufferView;

        public InternalBufferView(
                SliceBufferNew<A>.BufferWithAutoReloadOnAccess baseBuffer,
                RangeBuffer<A> rangeBufferView) {
            super();
            this.baseBuffer = baseBuffer;
            this.rangeBufferView = rangeBufferView;
        }

        public BufferWithAutoReloadOnAccess getBaseBuffer() {
            return baseBuffer;
        }

        @Override
        public RangeBuffer<A> getRangeBuffer() {
            return rangeBufferView;
        }

        @Override
        public long getGeneration() {
            return baseBuffer.getGenerationHere();
        }

        @Override
        public String toString() {
            return rangeBufferView.toString();
        }

        @Override
        public long getCapacity() {
            return pageSize;
        }

        @Override
        public ReadWriteLock getReadWriteLock() {
            return readWriteLock;
        }
    }

    class BufferWithAutoReloadOnAccess
        implements BufferDelegate<A>
    {
        protected String fileName;
        protected int generationHere;
        protected CompletableFuture<Buffer<A>> future;

        public BufferWithAutoReloadOnAccess(String fileName) {
            super();
            this.fileName = fileName;
            this.generationHere = -1;
        }

        public int getGenerationHere() {
            return generationHere;
        }

        public CompletableFuture<Buffer<A>> reloadIfNeeded() {
            int generationNow = SliceBufferNew.this.liveGeneration;
            if (generationHere != generationNow || future == null) {
                synchronized (this) {
                    if (generationHere != generationNow || future == null) {
                        ObjectStoreConnection conn = objectStore.getConnection();
                        conn.begin(false);
                        generationHere = lockAndSyncMetaData(conn, pageSize);

                        ObjectResource pageRes = conn.access(objectStoreBasePath.resolve(fileName));

                        future = CompletableFuture.supplyAsync(() -> {
                            @SuppressWarnings("unchecked")
                            A array = (A)pageRes.loadNewInstance();
                            if (array == null) {
                                array = arrayOps.create(pageSize);
                            }

                            Buffer<A> arrayBuffer = ArrayBuffer.create(arrayOps, array);
                            return arrayBuffer;
                        }).whenComplete((v, t) -> {
                            try {
                                conn.commit();
                            } finally {
                                try {
                                    conn.close();
                                } catch (Exception e) {
                                    // TODO Because of async loading the exception may get swallowed - improve handling
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        return future;
                    }
                }
            }
            return future;
        }

        void updateIfNeeded(int generationNow, ObjectStoreConnection conn) {
            if (generationHere != generationNow || future == null) {
                ObjectResource pageRes = conn.access(objectStoreBasePath.resolve(fileName));

                A array = (A)pageRes.loadNewInstance();
                if (array == null) {
                    array = arrayOps.create(pageSize);
                }

                Buffer<A> arrayBuffer = ArrayBuffer.create(arrayOps, array);
                future = CompletableFuture.completedFuture(arrayBuffer);
                generationHere = generationNow;
            }
        }

        @Override
        public Buffer<A> getDelegate() {
            try {
                return future.get();
                // return reloadIfNeeded().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException();
            }
        }
    }

    @Override
    public RangeMap<Long, List<Throwable>> getFailedRanges() {
        return liveMetaData.getFailedRanges();
    }
}


//public RefFuture<BufferView<A>> getPageForPageId(long pageId) {
//String fileName = pageIdToFileName.apply(pageId);
//
//return pageCache.claim(fileName);
//}
//
//// @Override
//public RefFuture<BufferView<A>> getPageForPageId2Old(long pageId) {
//// lock the file and derialize its content
//
//
//// Buffer<A> overlay = new ArrayBuffer<>(arrayOps, arrayOps.create(pageSize)); // new PagedBuffer<>(arrayOps, pageSize);
//
//String fileName = pageIdToFileName.apply(pageId);
//
//BufferWithAutoReload<A> baseBuffer = new BufferWithAutoReload<>(
//		objectStore,
//		fileName,
//		arrayOps,
//		pageSize,
//		-1,
//		() -> this.liveGeneration);
//baseBuffer.reload();
//
//
//RefFuture<ObjectInfo> ref = objectStore.claim(fileName);
//
//RefFuture<BufferView<A>> result = ref.acquireTransformed(oi -> {
//
//	int generationHere = liveGeneration;
//
//
//	// Set up a base buffer that auto-reloads on access if the generation here does not match the one of the slice anymore
//	Buffer<A> baseDelegate = new BufferDelegate<A>() {
//		@Override
//		public Buffer<A> getDelegate() {
//			int generationHere = 0;
//			if (liveGeneration != generationHere) {
//
//			}
//
//			return null;
//		}
//	};
//
//
//	A array = oi.getObject();
//	Buffer<A> buffer = array == null
//			? ArrayBuffer.create(arrayOps, pageSize)
//			: ArrayBuffer.create(arrayOps, array);
//
//	long pageOffset = PageUtils.getPageOffsetForId(pageId, pageSize);
//
//	// baseMetaData.getLoadedRanges()
//	// RangeBuffer<A> baseBuffer = RangeBufferImpl.create(baseRanges, pageOffset, buffer);
//	RangeBuffer<A> deltaBuffer1 = syncChanges.slice(pageOffset, pageSize);
//	RangeBuffer<A> deltaBuffer2 = liveChanges.slice(pageOffset, pageSize);
//
//	RangeBuffer<A> unionBuffer = RangeBufferUnion.create(baseBuffer, deltaBuffer1);
//	RangeBuffer<A> finalUnionBuffer = RangeBufferUnion.create(deltaBuffer2, unionBuffer);
//
//	// Wrap the buffer such that the minimum known size is updated?
//
//
//	return new BufferView<A>() {
//		@Override
//		public RangeBuffer<A> getRangeBuffer() {
//			return finalUnionBuffer;
//		}
//
////		@Override
////		public ReadWriteLock getReadWriteLock() {
////			return readWriteLock;
////		}
//
//		@Override
//		public long getGeneration() {
//			return liveGeneration;
//		}
//
//		@Override
//		public String toString() {
//			return finalUnionBuffer.toString();
//		}
//	};
//});
//ref.close();
//
//
//return result;
//}
