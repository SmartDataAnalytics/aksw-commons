package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.aksw.commons.kyro.guava.EntrySerializer;
import org.aksw.commons.kyro.guava.RangeMapSerializer;
import org.aksw.commons.kyro.guava.RangeSetSerializer;
import org.aksw.commons.rx.range.RangedSupplier;
import org.aksw.commons.store.object.key.api.KeyObjectStore;
import org.aksw.commons.store.object.key.impl.KeyObjectStoreImpl;
import org.aksw.commons.store.object.path.impl.ObjectFileStoreKyro;
import org.aksw.commons.util.range.BufferWithGeneration;
import org.aksw.commons.util.range.CountInfo;
import org.aksw.commons.util.range.RangeBuffer;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.commons.util.slot.Slot;
import org.aksw.jena_sparql_api.lookup.ListPaginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;
import com.google.common.math.LongMath;
import com.google.common.util.concurrent.MoreExecutors;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;


public class SmartRangeCacheImpl<T>
    implements ListPaginator<T>
{
    private static final Logger logger = LoggerFactory.getLogger(SmartRangeCacheImpl.class);

    /** The supplier for actually retrieving data from the backend */
    protected ListPaginator<T> backend;

    protected SliceWithPages<T> slice;


    protected Single<Range<Long>> countSingle;

    // protected int pageSize;
    // protected AsyncClaimingCache<Long, RangeBuffer<T>> pageCache;

    /**
     * Reuse of the caching infrastructure for a single entry for the count.
     * The infrastructure enables dealing with concurrent requests and synchronization
     */
    // protected AsyncClaimingCache<String, Range<Long>> countCache;

    // protected AsyncClaimingCache<String, SliceMetaData> metadataCache;


    // protected AsyncClaimingCache<String, Object> metadataCache;


    protected long requestLimit;
    protected long terminationDelayInMs;


    protected ExecutorService executorService =
            MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)Executors.newCachedThreadPool());



    // protected SortedCache<Long, RangeBuffer<T>> pageCache;

    protected Set<RangeRequestWorker<T>> executors = Collections.synchronizedSet(Sets.newIdentityHashSet());


    protected Set<RangeRequestIterator<T>> activeRequests = Collections.synchronizedSet(Sets.newIdentityHashSet());

    // protected ReentrantReadWriteLock executorCreationLock = new ReentrantReadWriteLock(true);

    // protected volatile long knownSize = -1;



    public SmartRangeCacheImpl(
            ListPaginator<T> backend,
            KeyObjectStore objStore,
            int pageSize,
            long maxCachedPageCount,
            Duration syncDelayDuration,
            long requestLimit,
            long terminationDelayInMs) {
        this.backend = backend;

        this.slice = new SliceWithPagesImpl<>(
                objStore,
                pageSize,
                maxCachedPageCount,
                syncDelayDuration);

        this.requestLimit = requestLimit;
        this.terminationDelayInMs = terminationDelayInMs;

        this.countSingle = backend
                .fetchCount(null, null)
                .map(r -> {
                    CountInfo countInfo = RangeUtils.toCountInfo(r);
                    if (!countInfo.isHasMoreItems()) {
                        long count = countInfo.getCount();

                        slice.mutateMetaData(metaData -> metaData.setKnownSize(count));
                    }
                    return r;
                })
                .cache();

    }

    public RangedSupplier<Long, T> getBackend() {
        return backend;
    }

//    public void setKnownSize(long knownSize) {
//        this.knownSize = knownSize;
//    }

    public SliceWithPages<T> getSlice() {
        return slice;
    }




    public Set<RangeRequestWorker<T>> getExecutors() {
        return executors;
    }


    Lock getExecutorCreationReadLock() {
        return slice.getWorkerCreationLock();
        // return executorCreationLock.readLock();
    }


//    public long getPageIdForOffset(long offset) {
//        long result = offset / pageSize;
//        return result;
//    }
//
//    /**
//     * This method should only be called by producers.
//     *
//     * This method triggers loads of non-loaded pages which in turn
//     *
//     *
//     * */
//    public RefFuture<RangeBuffer<T>> getPageForOffset(long offset) {
//        long pageId = getPageIdForOffset(offset);
//        return getPageForPageId(pageId);
//    }
//
//    public RefFuture<RangeBuffer<T>> getPageForPageId(long pageId) {
//        RefFuture<RangeBuffer<T>> result;
//        try {
//            result = pageCache.claim(pageId);
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//        return result;
//    }
//
//    public int getIndexInPageForOffset(long offset) {
//        return (int)(offset % (long)pageSize);
//    }



    public Runnable register(RangeRequestIterator<T> it) {
        activeRequests.add(it);

        return () -> {
            activeRequests.remove(it);
        };
    }



    // protected Set<BiConsumer<Long, RangeBufferImpl<T>>> pageLoadListeners = Collections.synchronizedSet(Sets.newIdentityHashSet());

//    public Runnable addPageLoadListener(BiConsumer<Long, RangeBufferImpl<T>> listener) {
//        pageLoadListeners.add(listener);
//        return () -> pageLoadListeners.remove(listener);
//    }


    /** The open requests sorted by the start of their lowest GAP! (not the original request offset)
     *  I.e. requests are indexed by their first position where backend data retrieval must be performed
     */
    // protected TreeMultimap<Long, RequestContext> openRequests;


    /**
     * Map of the next interceptable executor offset to the executor
     * Executors regularly synchronize on this map to declare the offset on which they check
     *
     *
     */
    protected NavigableMap<Long, Executor> offsetToExecutor = new TreeMap<>();
    // protected ConcurrentNavigableMap<Long, Executor> offsetToExecutor = new ConcurrentSkipListMap<>();

    /// protected RangeMap<Long, Object> autoClaimers;



    public Entry<RangeRequestWorker<T>, Slot<Long>> newExecutor(long offset, long initialLength) {
        // RangeRequestExecutor<T> result;
        RangeRequestWorker<T> worker;
        Slot<Long> slot;
        //executorCreationLock.writeLock().lock();
        try {
            worker = new RangeRequestWorker<>(this, offset, requestLimit, terminationDelayInMs);
            slot = worker.getEndpointSlot();
            slot.set(offset + initialLength);

            executors.add(worker);
            logger.debug("NEW WORKER: " + offset + ":" + initialLength);
            executorService.submit(worker);
        } finally {
            // executorCreationLock.writeLock().unlock();
        }
        return new SimpleEntry<>(worker, slot);
    }


    /**
     * Create a RequestContext for the given requestRange:
     *
     * (1) Claim cached pages for the start-range of the request range
     * (2) Check the running executors for whether they are suitable for (partially) servinge the request range
     *     If so, assign tasks to those executors
     * (3) If no running executor is suitable then add the request to the 'pending queue'
     *
     * If the executor service
     *
     *
     * @param requestRange
     */
    public RangeRequestIterator<T> request(Range<Long> requestRange) {

        RangeRequestIterator<T> result = new RangeRequestIterator<>(this, requestRange);

        return result;
//
//
//		RangeRequestContext<Long> cxt = new RangeRequestContext<Long>(requestRange);
//
//		// Claim existing pages in the given range and register a listener
//		// that auto-claims any pages that become available
//		synchronized (pageCache) {
//			RangeMap<Long, Ref<RangeBuffer<T>>> claims = pageCache.claimAll(requestRange);
//
//			RangeSet<Long> ranges = claims.asMapOfRanges().keySet();
//			RangeSet<Long> gaps = RangeUtils.gaps(ranges, requestRange);
//
//			if (!gaps.isEmpty()) {
//				// Check whether there are already executor contexts to which the requests can be added
//
//
//
//
//			}
//		}


    }


    public Flowable<T> apply(Range<Long> range) {

        return Flowable.generate(
            () -> request(range),
            (it, e) -> {
                if (it.hasNext()) {
                    T item = it.next();
                    e.onNext(item);
                } else {
                    e.onComplete();
                }
            },
            RangeRequestIterator::close);
    }


    public static <V> ListPaginator<V> wrap(
            ListPaginator<V> backend,
            KeyObjectStore store,
            int pageSize,
            long maxCachedPageCount,
            Duration syncDelayDuration,
            long requestLimit,
            long terminationDelayInMs) {
        return new SmartRangeCacheImpl<V>(
                backend, store, pageSize, maxCachedPageCount, syncDelayDuration, requestLimit, terminationDelayInMs);
    }

    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {

        Single<Range<Long>> result;

        long knownSize = slice.computeFromMetaData(SliceMetaData::getKnownSize);


        if (knownSize >= 0) {
            result = Single.just(Range.singleton(knownSize));
        } else {
            result = countSingle;

//            result = backend
//                .fetchCount(null, null)
//                .map(r -> {
//                    CountInfo countInfo = RangeUtils.toCountInfo(r);
//                    if (!countInfo.isHasMoreItems()) {
//                        long count = countInfo.getCount();
//
//                        slice.mutateMetaData(metaData -> metaData.setKnownSize(count));
//                    }
//                    return r;
//                })
//                .cache();

        }
//        Single<Range<Long>> result = Flowable.<Range<Long>, SimpleEntry<RefFuture<Range<Long>>, Boolean>>generate(
//                () -> new SimpleEntry<RefFuture<Range<Long>>, Boolean>(countCache.claim("count"), false),
//                (ee, e) -> {
//                    try {
//                        if (!ee.getValue()) {
//                            Range<Long> range = ee.getKey().await();
//                            e.onNext(range);
//                            ee.setValue(true);
//                        } else {
//                            e.onComplete();
//                        }
//                    } catch (Exception x) {
//                        e.onError(x);
//                    }
//                },
//                ee -> ee.getKey().close())
//                .singleOrError();
//
//        return result;


        // If the size is known then return it - otherwise send at most one query to the backend for counting
        // and return the result.
        // Extension: If the count becomes known before the query returns then we could abort the count-request
        // and return the newly known value instead
//		if (knownSize < 0) {
//			return
//		} else {
//			back
//		}

        return result;
    }



    public static KryoPool createKyroPool(Consumer<Kryo> customRegistrator) {
        KryoFactory factory = new KryoFactory() {
            public Kryo create() {
                Kryo kryo = new Kryo();

                Serializer<?> javaSerializer = new JavaSerializer();
                Serializer<?> rangeSetSerializer = new RangeSetSerializer();
                Serializer<?> rangeMapSerializer = new RangeMapSerializer();
                Serializer<?> entrySerializer = new EntrySerializer();

                kryo.register(TreeRangeSet.class, rangeSetSerializer);
                kryo.register(TreeRangeMap.class, rangeMapSerializer);
                kryo.register(Range.class, javaSerializer);
                kryo.register(SimpleEntry.class, entrySerializer);

                if (customRegistrator != null) {
                    customRegistrator.accept(kryo);
                }
                return kryo;
            }
        };
        // Build pool with SoftReferences enabled (optional)
        KryoPool result = new KryoPool.Builder(factory).softReferences().build();
        return result;
    }

    public static KeyObjectStore createKeyObjectStore(Path basePath, KryoPool kryoPool) {
        KeyObjectStore result = KeyObjectStoreImpl.create(basePath, new ObjectFileStoreKyro(kryoPool));

        return result;
    }


    public static <T> Deque<Range<Long>> computeGaps(Range<Long> requestRange, long pageSize,
            NavigableMap<Long, RangeBuffer<T>> pages) {

        // Range<Long> totalRange = Range.closedOpen(pageOffset, pageOffset + pageSize);


        RangeSet<Long> loadedRanges = TreeRangeSet.create();
        pages.entrySet().stream().flatMap(e -> e.getValue().getLoadedRanges().asRanges().stream().map(range ->
                RangeUtils.apply(
                        range,
                        e.getKey() * pageSize,
                        (endpoint, value) -> LongMath.saturatedAdd(endpoint, (long)value)))
            )
            .forEach(loadedRanges::add);


        RangeSet<Long> rawGaps = RangeUtils.gaps(requestRange, loadedRanges);
        Deque<Range<Long>> gaps = new ArrayDeque<>(rawGaps.asRanges());
        return gaps;
    }


//    public PageRange<T> newPageRange() {
//        return new PageRangeImpl<>(this);
//    }


    public static <V> AsyncClaimingCache<Long, Entry<RangeBuffer<V>, Long>> syncedRangeBuffer(
            long maximumSize,
            Duration syncDelayDuration,
            KeyObjectStore store,
            Supplier<RangeBuffer<V>> newValue) {

        // A map that upon loading a page takes a snapshot of its generation attribute
        // and treats it as the version
        // A page is dirty if its generation differs from its version
        // Map<Long, Long> keyToVersion = Collections.synchronizedMap(new IdentityHashMap<>());

        AsyncClaimingCache<Long, Entry<RangeBuffer<V>, Long>> result = AsyncClaimingCacheImpl.create(
                syncDelayDuration,

                // begin of level3 setup
                   Caffeine.newBuilder()
                   // .scheduler(Scheduler.systemScheduler())
                   .maximumSize(maximumSize), //.expireAfterWrite(1, TimeUnit.SECONDS),
                   key -> {
                       List<String> internalKey = Arrays.asList(Long.toString(key));
                       RangeBuffer<V> value;
                       try {
                           value = store.computeIfAbsent(internalKey, newValue::get);
                       } catch (Exception e) {
                           // logger.warn("Error", e);
                           throw new RuntimeException(e);
                          //  value = newValue.get(); //new RangeBufferImpl<V>(1024);
                       }

//                       RangeBuffer<V> r = value;
                       long generation = value.getGeneration();
                       Entry<RangeBuffer<V>, Long> r = new SimpleEntry<>(value, generation);

                       // keyToVersion.put(key, generation);

    //                   Ref<V> r = RefImpl.create(v, null, () -> {
    //                       // Sync the page upon closing it
    //                       store.put(internalKey, v);
    //                       logger.info("Synced " + internalKey);
    //                       System.out.println("Synced" + internalKey);
    //                   });
    //
                       return r;

                   },
                   (key, value, cause) -> {
                       // keyToVersion.remove(key);
                   },
                // end of level3 setup

                (key, value, cause) -> {
                    RangeBuffer<V> buffer = value.getKey();
                    Long version = value.getValue();

                    List<String> internalKey = Arrays.asList(Long.toString(key));

                    Lock readLock = buffer.getReadWriteLock().readLock();
                    readLock.lock();

                    // Long version = keyToVersion.get(key);

                    if (version == null) {
                        logger.error("Missing version for [" + key + "]");
                    }

                    long generation = buffer.getGeneration();

                    try {
                        if (generation != version) {
                            logger.info("Syncing dirty buffer " + internalKey);
                            // keyToVersion.put(key, generation);
                            store.put(internalKey, buffer);
                            value.setValue(version);
                        } else {
                            logger.info("Syncing not needed because of clean buffer " + internalKey);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        readLock.unlock();
                    }
                }
            );

        return result;
    }


    public static <V> AsyncClaimingCache<Long, Entry<BufferWithGeneration<V>, Long>> syncedBuffer(
            long maximumSize,
            Duration syncDelayDuration,
            KeyObjectStore store,
            Supplier<BufferWithGeneration<V>> newValue) {

        // A map that upon loading a page takes a snapshot of its generation attribute
        // and treats it as the version
        // A page is dirty if its generation differs from its version
        // Map<Long, Long> keyToVersion = Collections.synchronizedMap(new IdentityHashMap<>());

        AsyncClaimingCache<Long, Entry<BufferWithGeneration<V>, Long>> result = AsyncClaimingCacheImpl.create(
                syncDelayDuration,

                // begin of level3 setup
                   Caffeine.newBuilder()
                   // .scheduler(Scheduler.systemScheduler())
                   .maximumSize(maximumSize), //.expireAfterWrite(1, TimeUnit.SECONDS),
                   key -> {
                       List<String> internalKey = Arrays.asList(Long.toString(key));
                       BufferWithGeneration<V> value;
                       try {
                           value = store.computeIfAbsent(internalKey, newValue::get);
                       } catch (Exception e) {
                           // logger.warn("Error", e);
                           throw new RuntimeException(e);
                          //  value = newValue.get(); //new RangeBufferImpl<V>(1024);
                       }

//                       RangeBuffer<V> r = value;
                       long generation = value.getGeneration();
                       Entry<BufferWithGeneration<V>, Long> r = new SimpleEntry<>(value, generation);

                       // keyToVersion.put(key, generation);

    //                   Ref<V> r = RefImpl.create(v, null, () -> {
    //                       // Sync the page upon closing it
    //                       store.put(internalKey, v);
    //                       logger.info("Synced " + internalKey);
    //                       System.out.println("Synced" + internalKey);
    //                   });
    //
                       return r;

                   },
                   (key, value, cause) -> {
                       // keyToVersion.remove(key);
                   },
                // end of level3 setup

                (key, value, cause) -> {
                    BufferWithGeneration<V> buffer = value.getKey();
                    Long version = value.getValue();

                    List<String> internalKey = Arrays.asList(Long.toString(key));

                    Lock readLock = buffer.getReadWriteLock().readLock();
                    readLock.lock();

                    // Long version = keyToVersion.get(key);

                    if (version == null) {
                        logger.error("Missing version for [" + key + "]");
                    }

                    long generation = buffer.getGeneration();

                    try {
                        if (generation != version) {
                            logger.info("Syncing dirty buffer " + internalKey);
                            // keyToVersion.put(key, generation);
                            store.put(internalKey, buffer);
                            value.setValue(version);
                        } else {
                            logger.info("Syncing not needed because of clean buffer " + internalKey);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        readLock.unlock();
                    }
                }
            );

        return result;
    }


}
