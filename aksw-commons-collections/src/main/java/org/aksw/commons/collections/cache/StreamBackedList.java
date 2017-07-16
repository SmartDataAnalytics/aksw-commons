package org.aksw.commons.collections.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StreamBackedList<T>
    extends CacheImpl<T>
{
    private static final Logger logger = LoggerFactory.getLogger(StreamBackedList.class);

    protected Stream<T> stream;
    protected Future<?> future;

    public StreamBackedList(Stream<T> stream) {
        this(stream, Executors.newSingleThreadExecutor(), true);
    }

    public StreamBackedList(Stream<T> stream, ExecutorService es) {
        this(stream, es, false);
    }

    public StreamBackedList(Stream<T> stream, ExecutorService es, boolean shutdownExecutorService) {
        super(new ArrayList<>());

        this.stream = stream;

        future = es.submit(() -> {
            try {
                Iterator<T> it = stream.iterator();
                while(it.hasNext() && !Thread.interrupted()) {
//                    logger.info("About to read an item.");
                    T item = it.next();
//                    logger.info("Read: " + item);
                    this.add(item);
//                    logger.info("Added: " + item);
                }
//                logger.info("Complete");
                this.setComplete();
            } catch(Exception e) {
                logger.warn("Unexpected error", e);
                this.setAbandoned();
            } finally {
                try {
                    stream.close();
                    super.close();
                } catch (Exception e) {
                    logger.warn("Unexpected error", e);
                }
            }
        });

        if(shutdownExecutorService) {
            es.shutdown();
        }
    }

    @Override
    public void close() throws Exception {
        // Note: Cancelling the future will close the stream as a side effect
        // Attempt to get the future's value - this will expose any exception fired during async processing
        try {
            // Note: If the cache is complete, the future should become done shortly afterwards - therefore
            // the possibly block by future.get() should never cause a deadlock
            if(isComplete()) {
                future.get();
            } else {
                future.cancel(true);
            }
        } catch(Exception e) {
            logger.warn("Unexpected error", e);
        }

//        } finally {
//            stream.close();
//        }
    }
}
