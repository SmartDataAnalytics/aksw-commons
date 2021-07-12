package org.aksw.commons.rx.cache.range;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

import org.aksw.commons.rx.range.KeyObjectStore;
import org.aksw.commons.rx.range.RangedSupplier;
import org.aksw.commons.util.range.RangeBuffer;
import org.aksw.commons.util.range.RangeBufferImpl;
import org.aksw.commons.util.ref.RefFuture;
import org.aksw.commons.util.slot.Slot;
import org.aksw.jena_sparql_api.lookup.ListPaginator;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

interface PageManager<T> {
    // Reference<? extends Page<T>>
    Page<T> getPage(long pageId);
    int getPageSize();
}


interface Page<T> {
    long getOffset();
    PageManager<T> getPageManager();

    T get(int offset);
    void set(int offset, T value);
    int getKnownSize();

    default Page<T> getNextPage() {
        return getPageManager().getPage(getOffset() + 1);
    }
}





class RangeRequestContext<C extends Comparable<C>> {
    protected Range<C> range;

    public RangeRequestContext(Range<C> range) {
        super();
        this.range = range;
    }

    public Range<C> getRange() {
        return range;
    }
}


class ExecutorPool {
    // protected
    protected Set<Range<Long>> requestRanges;


    public void addRequest(Range<Long> request) {

    }

}


public class SmartRangeCacheImpl<T>
    implements ListPaginator<T>
{
    /** The supplier for actually retrieving data from the backend */
    protected RangedSupplier<Long, T> backend;

    protected int pageSize;
    protected AsyncClaimingCache<Long, RangeBuffer<T>> pageCache;


    protected ExecutorService executorService =
            MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)Executors.newCachedThreadPool());



    // protected SortedCache<Long, RangeBuffer<T>> pageCache;

    protected Set<RangeRequestExecutor<T>> executors = Collections.synchronizedSet(Sets.newIdentityHashSet());


    protected Set<RequestIterator<T>> activeRequests = Collections.synchronizedSet(Sets.newIdentityHashSet());

    protected ReentrantReadWriteLock executorCreationLock = new ReentrantReadWriteLock(true);

    protected volatile long knownSize = -1;



    public SmartRangeCacheImpl(RangedSupplier<Long, T> backend, KeyObjectStore objStore) {
        this.backend = backend;

        pageSize = 1024;


        pageCache = LocalOrderAsyncTest.syncedRangeBuffer(objStore, () -> new RangeBufferImpl<T>(pageSize));
//        new ClaimingCache<>(
//                CacheBuilder.newBuilder()
//                    .maximumSize(1000)
//                    .build(new CacheLoader<Long, RangeBufferImpl<T>>() {
//                        @Override
//                        public RangeBufferImpl<T> load(Long key) throws Exception {
//                            RangeBufferImpl<T> result = new RangeBufferImpl<T>(pageSize);
//                            onPageLoad(key, result);
//                            return result;
//                        }
//                    }),
//                new TreeMap<>()
//        );
    }

    public void setKnownSize(long knownSize) {
        this.knownSize = knownSize;
     }

    public int getPageSize() {
        return pageSize;
    }


    /**
     * Listener on page loads that auto-claims pages to RequestIterators
     * Note that the listener is invoked before the entry can be registered at the cache -
     * cache lookups must not be performed in the listener's flow of control.
     */
    protected void onPageLoad(Long key, RangeBufferImpl<T> page) {

    }


    public Set<RangeRequestExecutor<T>> getExecutors() {
        return executors;
    }


    Lock getExecutorCreationReadLock() {
        return executorCreationLock.readLock();
    }


    public long getPageIdForOffset(long offset) {
        long result = offset / pageSize;
        return result;
    }

    /**
     * This method should only be called by producers.
     *
     * This method triggers loads of non-loaded pages which in turn
     *
     *
     * */
    public RefFuture<RangeBuffer<T>> getPageForOffset(long offset) {
        long pageId = getPageIdForOffset(offset);
        return getPageForPageId(pageId);
    }

    public RefFuture<RangeBuffer<T>> getPageForPageId(long pageId) {
        RefFuture<RangeBuffer<T>> result;
        try {
            result = pageCache.claim(pageId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public int getIndexInPageForOffset(long offset) {
        return (int)(offset % (long)pageSize);
    }



    public Runnable register(RequestIterator<T> it) {
        activeRequests.add(it);

        return () -> {
            activeRequests.remove(it);
        };
    }



    protected Set<BiConsumer<Long, RangeBufferImpl<T>>> pageLoadListeners = Collections.synchronizedSet(Sets.newIdentityHashSet());

    public Runnable addPageLoadListener(BiConsumer<Long, RangeBufferImpl<T>> listener) {
        pageLoadListeners.add(listener);
        return () -> pageLoadListeners.remove(listener);
    }


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



    public Slot<Long> newExecutor(long offset, long initialLength) {
        // RangeRequestExecutor<T> result;
        Slot<Long> slot;
        executorCreationLock.writeLock().lock();
        try {
            RangeRequestExecutor<T> worker = new RangeRequestExecutor<>(offset);
            slot = worker.getEndpointSlot();
            slot.set(offset + initialLength);

            executors.add(worker);
            executorService.submit(worker);
        } finally {
            executorCreationLock.writeLock().unlock();
        }
        return slot;
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
    public RequestIterator<T> request(Range<Long> requestRange) {

        RequestIterator<T> result = new RequestIterator<>(this);

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
            RequestIterator::close);
    }


    public static <V> ListPaginator<V> wrap(RangedSupplier<Long, V> backend, KeyObjectStore store) {
        return new SmartRangeCacheImpl<V>(backend, store);
    }

    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
        // If the size is known then return it - otherwise send at most one query to the backend for counting
        // and return the result.
        // Extension: If the count becomes known before the query returns then we could abort the count-request
        // and return the newly known value instead
        throw new UnsupportedOperationException("not implemented yet");
//		if (knownSize < 0) {
//			return
//		} else {
//			back
//		}

    }

}
