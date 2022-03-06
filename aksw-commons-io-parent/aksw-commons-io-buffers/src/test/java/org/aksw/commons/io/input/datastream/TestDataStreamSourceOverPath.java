package org.aksw.commons.io.input.datastream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.cache.AdvancedRangeCacheConfigImpl;
import org.aksw.commons.io.input.DataStream;
import org.aksw.commons.io.input.DataStreamSource;
import org.aksw.commons.io.input.DataStreamSources;
import org.aksw.commons.io.input.DataStreams;
import org.aksw.commons.io.slice.SliceInMemoryCache;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

public class TestDataStreamSourceOverPath {
    private static final Logger logger = LoggerFactory.getLogger(TestDataStreamSourceOverPath.class);

    @Test
    public void test() throws IOException {
        File file = File.createTempFile("aksw-commons-", ".dat");
        file.deleteOnExit();
        try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
            for (int i = 0; i < 100000; ++i) {
                out.println("item #" + i);
            }
            out.flush();
        }

        Path path = file.toPath();
        int size = Ints.saturatedCast(Files.size(path));

        logger.info("Created test data file (will be deleted on exit) " + path + " of size " + size);

        DataStreamSource<byte[]> source = DataStreamSources.of(path);
        DataStreamSource<byte[]> cached = DataStreamSources.cache(
                source,
                SliceInMemoryCache.create(ArrayOps.BYTE, 4096, 100), AdvancedRangeCacheConfigImpl.createDefault());

        Random random = new Random();

        for (int i = 0; i < 100; ++i) {
            Stopwatch sw = Stopwatch.createStarted();

            int start = random.nextInt(size);
            int len = Math.min(random.nextInt(size - start), 1024 * 128);
            Range<Long> range = Range.closedOpen((long)start, (long)(start + len));

            logger.debug("Request range: " + range + " (length: " + len + ")");

            try(
                DataStream<byte[]> sourceStream = source.newDataStream(range);
                DataStream<byte[]> cachedStream = cached.newDataStream(range)) {

                byte[] expected = IOUtils.toByteArray(DataStreams.newInputStream(sourceStream));
                byte[] actual = IOUtils.toByteArray(DataStreams.newInputStream(cachedStream));

                logger.debug(String.format("DataStreamSource uncached/cached comparison iteration %d took %f seconds", i, sw.elapsed(TimeUnit.MILLISECONDS) * 0.001f));

                Assert.assertArrayEquals(expected, actual);
            }
        }
    }
}
