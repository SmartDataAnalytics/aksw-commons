package org.aksw.commons.io.cache;

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

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.DataStream;
import org.aksw.commons.io.input.DataStreamSource;
import org.aksw.commons.io.slice.Slice;
import org.aksw.commons.util.slot.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;


public class AdvancedRangeCacheImpl<T>
    implements DataStreamSource<T>
{
    private static final Logger logger = LoggerFactory.getLogger(AdvancedRangeCacheImpl.class);

    protected DataStreamSource<T> dataSource;
    protected Slice<T> slice;

    // protected Set<RangeRequestIterator<T>> activeRequests = Collections.synchronizedSet(Sets.newIdentityHashSet());

    protected ReentrantLock workerCreationLock = new ReentrantLock();

    protected Set<RangeRequestWorkerImpl<T>> executors = Collections.synchronizedSet(Sets.newIdentityHashSet());

    protected long requestLimit;
    protected Duration terminationDelay;

    // Number of items a worker processes in bulk before signalling available data
    protected int workerBulkSize;

    protected ExecutorService executorService =
            MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)Executors.newCachedThreadPool());

    public AdvancedRangeCacheImpl(
            DataStreamSource<T> dataSource,
            Slice<T> slice,
            long requestLimit,
            int workerBulkSize,
            Duration terminationDelay) {

        this.dataSource = dataSource;

        this.slice = slice;
        this.requestLimit = requestLimit;
        this.workerBulkSize = workerBulkSize;
        this.terminationDelay = terminationDelay;
    }

    @Override
    public ArrayOps<T> getArrayOps() {
        return slice.getArrayOps();
    }

    public static <A> AdvancedRangeCacheImpl<A> create(
            DataStreamSource<A> dataSource,
            Slice<A> slice,
            long requestLimit,
            int workerBulkSize,
            Duration terminationDelay) {

        return new AdvancedRangeCacheImpl<>(dataSource, slice, requestLimit, workerBulkSize, terminationDelay);
    }


    public DataStreamSource<T> getDataSource() {
        return dataSource;
    }

    public Slice<T> getSlice() {
        return slice;
    }

    public Set<RangeRequestWorkerImpl<T>> getExecutors() {
        return executors;
    }


    /**
     * A lock that when held prevents creation of workers that put data into the slice.
     * This allows for analyzing all existing workers during scheduling; i.e. when deciding
     * whether for a data demand any new workers need to be created or existing ones can be reused.
     *
     */
    public Lock getExecutorCreationReadLock() {
        return workerCreationLock;
    }

//    public Runnable register(RangeRequestIterator<T> it) {
//        activeRequests.add(it);
//
//        return () -> {
//            activeRequests.remove(it);
//        };
//    }


    /**
     * Creates a new worker and immediately starts it.
     * The executor creation is driven by the RangeRequestIterator which creates executors on demand
     * whenever it detects any gaps in its read ahead range which are not served by any existing executors.
     *
     * @param offset
     * @param initialLength
     * @return
     */
    public Entry<RangeRequestWorkerImpl<T>, Slot<Long>> newExecutor(long offset, long initialLength) {
        RangeRequestWorkerImpl<T> worker;
        Slot<Long> slot;
        //executorCreationLock.writeLock().lock();
        try {
            worker = new RangeRequestWorkerImpl<>(this, offset, requestLimit, workerBulkSize, terminationDelay);
            slot = worker.newDemandSlot();
            slot.set(offset + initialLength);

//			if (offset == 63000000) {
//				System.out.println("debug point");
//			}
//			if (initialLength == 5) {
//				System.out.println("debug point");
//			}

            executors.add(worker);

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("New worker created with initial schedule of offset %1$d and length %2$d", offset, initialLength));
            }
            executorService.submit(worker);
        } finally {
            // executorCreationLock.writeLock().unlock();
        }
        return new SimpleEntry<>(worker, slot);
    }


    // Should only be called by the RangeRequestWorker once it terminates
    void removeExecutor(RangeRequestWorkerImpl<T> worker) {
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
    @Override
    public DataStream<T> newDataStream(Range<Long> range) {
        DataStreamOverSliceWithCache<T> result = new DataStreamOverSliceWithCache<>(this, range);
        // RangeRequestIterator<T> result = new RangeRequestIterator<>(this, requestRange);

        return result;
    }

    public static <A> Builder<A> newBuilder() {
        return new Builder<A>();
    }

    public static class Builder<A> {
        protected DataStreamSource<A> dataSource;
        protected Slice<A> slice;

        protected int workerBulkSize;

        protected long requestLimit;
        // protected Duration syncDelay;
        protected Duration terminationDelay;

        public DataStreamSource<A> getDataSource() {
            return dataSource;
        }

        public Builder<A> setDataSource(DataStreamSource<A> dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public Slice<A> getSlice() {
            return slice;
        }

        public Builder<A> setSlice(Slice<A> slice) {
            this.slice = slice;
            return this;
        }

        public long getRequestLimit() {
            return requestLimit;
        }

        public Builder<A> setRequestLimit(long requestLimit) {
            this.requestLimit = requestLimit;
            return this;
        }

        public int getWorkerBulkSize() {
            return workerBulkSize;
        }

        public Builder<A> setWorkerBulkSize(int workerBulkSize) {
            this.workerBulkSize = workerBulkSize;
            return this;
        }

        public Duration getTerminationDelay() {
            return terminationDelay;
        }

        public Builder<A> setTerminationDelay(Duration terminationDelay) {
            this.terminationDelay = terminationDelay;
            return this;
        }

//		public Duration getSyncDelay() {
//			return syncDelay;
//		}
//
//		public Builder<A> setSyncDelay(Duration syncDelay) {
//			this.syncDelay = syncDelay;
//			return this;
//		}

        public AdvancedRangeCacheImpl<A> build() {
            return AdvancedRangeCacheImpl.create(dataSource, slice, requestLimit, workerBulkSize, terminationDelay);
        }
    }

}

