package org.aksw.commons.io.input.datastream;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.cache.AdvancedRangeCacheConfig;
import org.aksw.commons.io.cache.AdvancedRangeCacheConfigImpl;
import org.aksw.commons.io.input.ReadableChannel;
import org.aksw.commons.io.input.ReadableChannelSource;
import org.aksw.commons.io.input.ReadableChannelSources;
import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.io.slice.SliceInMemoryCache;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

public class TestDataStreamSourceOverPath {
    private static final Logger logger = LoggerFactory.getLogger(TestDataStreamSourceOverPath.class);

    @Test
    public void testIteratorOverByteDataStream() {
        ReadableChannel<byte[]> xxx = ReadableChannels.of(ArrayOps.BYTE, new byte[] {'a', 'b', 'c'});
        Iterator<Byte> it = ReadableChannels.newBoxedIterator(xxx);
        while (it.hasNext()) {
            System.out.println((char)it.next().byteValue());
        }
    }


    /** Test case that generates an uncached DataSTreamSource over a file and then
     *  compares the result of range queries with the cache wrapper  */
    @Test
    public void testCachingOfDataStreamSources() throws IOException {
        Path tmpDir = Path.of(StandardSystemProperty.JAVA_IO_TMPDIR.value()).resolve("aksw-commons-tests");
        Files.createDirectories(tmpDir);

        Path testData = tmpDir.resolve("test-data.txt");
        if (!Files.exists(testData)) {
            try (PrintStream out = new PrintStream(Files.newOutputStream(testData))) {
                for (int i = 0; i < 100000; ++i) {
                    out.println("item #" + i);
                }
                out.flush();
            }
        }

        int size = Ints.saturatedCast(Files.size(testData));

        logger.info("Created test data file " + testData + " of size " + size);

        ReadableChannelSource<byte[]> source = ReadableChannelSources.of(testData, true);

        ReadableChannelSource<byte[]> cached;

        boolean useDisk = true;
        AdvancedRangeCacheConfig cacheConfig = AdvancedRangeCacheConfigImpl.newDefaultsForObjects();
        if (useDisk) {
            cached = ReadableChannelSources.cache(source, tmpDir, "filecache", cacheConfig);
        } else {
            cached = ReadableChannelSources.cache(
                    source,
                    SliceInMemoryCache.create(ArrayOps.BYTE, 4096, 100), AdvancedRangeCacheConfigImpl.newDefaultsForObjects());
        }

        Random random = new Random();

        for (int i = 0; i < 100; ++i) {
            Stopwatch sw = Stopwatch.createStarted();

            int start = random.nextInt(size);
            int len = Math.min(random.nextInt(size - start), 1024 * 128);
            Range<Long> range = Range.closedOpen((long)start, (long)(start + len));

            logger.debug("Request range: " + range + " (length: " + len + ")");

            try(
                ReadableChannel<byte[]> sourceStream = source.newReadableChannel(range);
                ReadableChannel<byte[]> cachedStream = cached.newReadableChannel(range)) {

                byte[] expected = IOUtils.toByteArray(ReadableChannels.newInputStream(sourceStream));
                byte[] actual = IOUtils.toByteArray(ReadableChannels.newInputStream(cachedStream));

                logger.debug(String.format("DataStreamSource uncached/cached comparison iteration %d took %f seconds", i, sw.elapsed(TimeUnit.MILLISECONDS) * 0.001f));

                Assert.assertArrayEquals(expected, actual);
            }
        }
    }
}
