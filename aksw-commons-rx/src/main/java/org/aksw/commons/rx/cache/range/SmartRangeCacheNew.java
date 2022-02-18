package org.aksw.commons.rx.cache.range;

import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.aksw.commons.util.slot.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;


public class SmartRangeCacheNew<T>
//     implements ListPaginator<T>
{
    private static final Logger logger = LoggerFactory.getLogger(SmartRangeCacheNew.class);

    protected SequentialReaderSource<T> dataSource;
    protected SliceWithPages<T> slice;

    protected Set<RangeRequestIterator<T>> activeRequests = Collections.synchronizedSet(Sets.newIdentityHashSet());
    
    protected ReentrantLock workerCreationLock = new ReentrantLock();

    protected Set<RangeRequestWorkerNew<T>> executors = Collections.synchronizedSet(Sets.newIdentityHashSet());
    
    protected long requestLimit;
    protected long terminationDelayInMs;


    protected ExecutorService executorService =
            MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)Executors.newCachedThreadPool());

    public SmartRangeCacheNew(
    		SequentialReaderSource<T> dataSource,
    		SliceWithPages<T> slice,
            Duration syncDelayDuration,
            long requestLimit,
            long terminationDelayInMs) {

    	this.dataSource = dataSource;

        this.slice = slice;
        this.requestLimit = requestLimit;
        this.terminationDelayInMs = terminationDelayInMs;
    }

    
    
    public SequentialReaderSource<T> getDataSource() {
		return dataSource;
	}
    
    public SliceWithPages<T> getSlice() {
        return slice;
    }

    public Set<RangeRequestWorkerNew<T>> getExecutors() {
        return executors;
    }

    public Lock getExecutorCreationReadLock() {
    	return workerCreationLock;
    }

    public Runnable register(RangeRequestIterator<T> it) {
        activeRequests.add(it);

        return () -> {
            activeRequests.remove(it);
        };
    }


    /**
     * Creates a new worker and immediately starts it.
     * The executor creation is driven by the RangeRequestIterator which creates executors on demand
     * whenever it detects any gaps in its read ahead range which are not served by any existing executors.
     * 
     * @param offset
     * @param initialLength
     * @return
     */
    public Entry<RangeRequestWorkerNew<T>, Slot<Long>> newExecutor(long offset, long initialLength) {
        RangeRequestWorkerNew<T> worker;
        Slot<Long> slot;
        //executorCreationLock.writeLock().lock();
        try {
            worker = new RangeRequestWorkerNew<>(this, offset, requestLimit, terminationDelayInMs);
            slot = worker.newDemandSlot();
            slot.set(offset + initialLength);

            executors.add(worker);
            logger.debug("NEW WORKER: " + offset + ":" + initialLength);
            executorService.submit(worker);
        } finally {
            // executorCreationLock.writeLock().unlock();
        }
        return new SimpleEntry<>(worker, slot);
    }

    
    // Should only be called by the RangeRequestWorker once it terminates
    void removeExecutor(RangeRequestWorker<T> worker) {
    	this.executors.remove(worker);
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
    public SequentialReader<T> request(Range<Long> requestRange) {

    	SequentialReaderFromSliceImpl<T> result = new SequentialReaderFromSliceImpl<>(this, requestRange);
        // RangeRequestIterator<T> result = new RangeRequestIterator<>(this, requestRange);

        return result;
    }

}

