package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.commons.io.binseach.BinarySearcher;
import org.aksw.commons.io.input.ReadableChannel;
import org.aksw.commons.io.input.ReadableChannelSupplier;
import org.aksw.commons.io.input.ReadableChannelWithLimitByDelimiter;
import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.io.input.SeekableReadableChannel;
import org.apache.hadoop.io.compress.BZip2Codec;

import com.google.common.base.Stopwatch;


public class BinCount {

    public static void main(String[] args) throws IOException {
//        System.out.println(String.format("%05d", 1));
//        if (true) { return; }

        // io error after 15min
        Path bzip2Path = Paths.get("/media/raven/T9/raven/datasets/wikidata/2024-08-24_wikidata-truthy.sorted.nt.bz2");
//        Path bzip2Path = Paths.get("/home/raven/Projects/Eclipse/linkedgeodata-parent/legacy/linkedgeodata-dump/target/dump/2018-04-04/2018-04-04-Amenity.node.sorted.nt.bz2");
//        Path plainPath = Paths.get("/media/raven/T9/raven/datasets/wikidata/2024-08-24_wikidata-truthy.sorted.nt");
         Path plainPath = Path.of("/home/raven/tmp/2018-04-04-Amenity.node.sorted.nt");

        BinarySearcher searcher = BinarySearchBuilder.newBuilder()
            .setSource(bzip2Path)
            .setCodec(new BZip2Codec())
            .build();
//        BinarySearcher searcher = BinarySearchBuilder.newBuilder()
//                .setSource(plainPath)
//                // .setCodec(new BZip2Codec())
//                .build();

        Stopwatch sw = Stopwatch.createStarted();
        List<ReadableChannelSupplier<byte[]>> splits = searcher.parallelSearch(null).toList();
        System.out.println("splits: " + splits.size());

        if (false) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(ReadableChannels.newInputStream(splits.get(1).newChannel())))) {
                br.lines().limit(1).forEach(x -> System.out.println(x));
                // long r = br.lines().count();
                //System.out.println("Contribution from split #x:" + r);
            }
        }

        if (true) {
            List<Entry<Integer, ReadableChannelSupplier<byte[]>>> indexedSplits = IntStream.range(0, splits.size()).mapToObj(i -> Map.entry(i, splits.get(i))).toList();

//              try (PrintWriter out = new PrintWriter(Files.newOutputStream(Path.of("/media/raven/T9/raven/datasets/wikidata/uncompressed.nt")), false, StandardCharsets.UTF_8)) {
                System.out.println("Count: " + indexedSplits.parallelStream().mapToLong(e -> {
                    int i = e.getKey();
                    ReadableChannelSupplier<byte[]> source = e.getValue();

                    long r = 0;
                    try {
    //                    try (PrintWriter out = new PrintWriter(Files.newOutputStream(Path.of("/media/raven/T9/raven/datasets/wikidata/split" + String.format("%05d", i) + ".nt")), false, StandardCharsets.UTF_8)) {
                             try (BufferedReader br = new BufferedReader(new InputStreamReader(ReadableChannels.newInputStream(source.newChannel()), StandardCharsets.UTF_8))) {
                                // r = br.lines().count();
                                String line;
                                while ((line = br.readLine()) != null) {
//                                    if (line.startsWith("<http://www.wikidata.org/entity/P2852>") && line.contains("/name")  && (line.contains("@bn"))) {
//                                        System.out.println("HERE:");
//                                        System.out.println(line);
//                                    }
//                                    if (line.startsWith("<http://www.wikidata.org/entity/P2852>") && line.contains("#label")  && (line.contains("@el") || line.contains("@sr"))) {
//                                        System.out.println("HERE:");
//                                        System.out.println(line);
//                                    }
//                                    out.println(line);
                                    ++r;
                                }
                                System.out.println("Contribution from split #x:" + r);
                            }
    //                        out.flush();
                        // }
                    }
                    catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    return r;
                }).sum());

//                out.flush();
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
        }

        System.out.println("Time Taken: " + (sw.elapsed(TimeUnit.MILLISECONDS) * 0.001));

    }

    public static void main2(String[] args) throws IOException {
        // Path path = Path.of("/home/raven/Projects/Eclipse/linkedgeodata-parent/legacy/linkedgeodata-dump/target/dump/2018-04-04/2018-04-04-Amenity.node.sorted.nt.bz2");

        // System.out.println((Byte.compare((byte)'>', (byte)'9')));

        Path path = Path.of("/media/raven/T9/raven/datasets/wikidata/2024-08-24_wikidata-truthy.sorted.nt.bz2");

        byte[] allPrefix = "<http://linkedgeodata.org/".getBytes();
        byte[] prefix = "<http://linkedgeodata.org/geometry/node1202810066>".getBytes(); // 1

        byte[] first = "<http://linkedgeodata.org/geometry/node1000036734>".getBytes();  // 0
        byte[] last = "<http://linkedgeodata.org/triplify/node999596437>".getBytes();    // 18807004


        byte[] wd = "<http://www.wikidata.org/entity/Q24075>".getBytes();

        byte[] lookup = wd;

        BlockSource blockSource = BlockSource.of(path, new BZip2Codec());

        // [0-44579]
        // [44580-44715]
        // [44716-89724]
        // [89725-89749]

        long pos = 1;
        // pos = 44579;
        // pos = 0;
        for (int x = 0; x < 0; ++x) {

            BlockSourceChannelAdapter channel = blockSource.newReadableChannel(pos, true);
            try (InputStream in = ReadableChannels.newInputStream(channel)) {
                long startBlockId = channel.getStartingBlockId();
                // channel.adjustToNextBlock();
                Long adjustedBlockId = channel.getCurrentBlockId();
                System.out.println(String.format("Transitioned from %d to %d and adjusted to %d", pos, startBlockId, adjustedBlockId));
                pos = startBlockId + 1;

                for (int i = 0; i < 5; ++i) {
                    long beforeBlkId = channel.getCurrentBlockId();
                    int c;
                    // System.out.println("  start block id: " + channel.getStartingBlockId());
                    long bytesRead = 0;
                    while ((c = in.read()) >= 0) { ++bytesRead; }
                    System.out.println(String.format("  iteration %d: block %d reached block %d with %d bytes", i, beforeBlkId, channel.getCurrentBlockId(), bytesRead));
                }
            }
        }

        // if (true) { return; }

        BinarySearcherOverBlockSource binSearcher = new BinarySearcherOverBlockSource(blockSource, BinSearchLevelCache.noCache(), 10000);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(binSearcher.search(lookup)))) {
            br.lines().forEach(x -> System.out.println(x));
        }
    }

//    public static void main3(String[] args) throws IOException {
//
//        Path path = Paths.get("/media/raven/T9/raven/datasets/wikidata/2024-08-24_wikidata-truthy.sorted.nt.bz2");
//        // Path path = Paths.get("/home/raven/Datasets/2024-08-24_wikidata-truthy.sorted.nt.zst");
//        // Path path = Paths.get("/media/raven/T9/raven/datasets/wikidata/2024-08-24_wikidata-truthy.sorted.nt");
//
//        BlockSource blockSource = BlockSource.of(path, new BZip2Codec());
//
//        int cpuCount = Runtime.getRuntime().availableProcessors();
//        System.out.println("cpus: " + cpuCount);
//        // cpuCount = 1;
//
//        long totalSize = blockSource.size();
//
//        // int splitCount = 10000; //cpuCount;
//        // long splitSize = totalSize / splitCount;
//
//
//        long splitSize = 50_000_000;
//        int evenSplitCount = (int)(totalSize / splitSize);
//        int splitCount = evenSplitCount + 1;
//
//
//        // long lastSplitSize = totalSize % splitSize;
//
//        // long lastSplitSize = totalSize - (evenSplitCount * splitSize);
//
//
//
//        System.out.println("Processing " + splitCount + " splits");
//
//
//        List<Integer> splitIds = IntStream.range(0, splitCount).boxed().toList();
//         // splitIds = splitIds.subList(10, 11);
///*
//        Transition to next block: 389394
//        Transition to next block: 389421
//  Line: <http://www.wikidata.org/entity/P10432> <http://wikiba.se/ontology#reference> <http://www.wikidata.org/prop/reference/P10432> .
//Line: <http://www.wikidata.org/entity/P10432> <http://wikiba.se/ontology#referenceValue> <http://www.wikidata.org/prop/reference/value/P10432> .
//Line: <http://www.wikidata.org/entity/P10432> <http://wikiba.se/ontology#referenceValueNormalized> <http://www.wikidata.org/prop/reference/value-normalized/P10432> .
//Line: <http://www.wikidata.org/entity/P10432> <http://wikiba.se/ontology#statementProperty> <http://www.wikidata.org/prop/statement/P10432> .
//Line: <http://www.wikidata.org/entity/P10432> <http://wikiba.se/ontology#statementValue> <http://www.wikidata.org/prop/statement/value/P10432> .
//
//        */
//        try (Stream<String> lines = splitIds.parallelStream().flatMap(splitId -> {
//            long start = splitId * splitSize;
//
//            boolean isLastSplit = splitId == splitCount - 1;
//            long end = isLastSplit ? totalSize : start + splitSize;
//
//            SeekableReadableChannel<byte[]> coreChannel;
//            try {
//                coreChannel = blockSource.newReadableChannel(start);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            ReadableChannel<byte[]> channel = new ReadableChannelWithLimitByDelimiter<>(coreChannel, coreChannel::position, (byte)'\n', end);
//            BufferedReader br = new BufferedReader(new InputStreamReader(ReadableChannels.newInputStream(channel), StandardCharsets.UTF_8));
//            int skipCount = splitId == 0 ? 0 : 1; // Skip the first line on all splits but the first
//
//            return br.lines().skip(skipCount).onClose(() -> {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        })) {
//            // lines.limit(10000).forEach(x -> System.out.println("Line: " + x));
//
//            System.out.println("Count: " + lines.count());
//        }
//
//        if (true) {
//            return;
//        }
//
//        /*
//         * index structure: keep track of the first record (e.g. triple/quad/line) in each block.
//         *
//         * search:
//         * - obtain a position p1
//         * - set the channel to it
//         * - read 1 byte, then take the position p2, unread the byte (should be a helper function)
//         *   now we know at which position the channel is positioned.
//         *   sanity check: p1 should differ from p2 unless we hit a block offset
//         *
//         * - skip over the first record (newline), then start a parser on the remaining stream.
//         * - take the first item from the parser, add it to the cache metadata.
//         * - use it to decide whether to search in the block left or right.
//         *   if the id matches, we still need to find the starting point, which is left.
//         *
//         * searching within blocks:
//         *   for decoded data we need to lead the whole block
//         *
//         *
//         */
//
//        //pos = 20133431795l;
//        // pos = 20133431796l;
////        SeekableByteChannel coreChannel = blockSource.newChannel();
////        coreChannel.position(pos);
////
////
////        long finalPos = pos;
////
////        try (ReadableByteChannel channel = new ReadableByteChannelWithLimitByNewline<>(
////                coreChannel, pos + 100000)) {
////            System.out.println(coreChannel.position());
////
////            try (BufferedReader br = new BufferedReader(new InputStreamReader(Channels.newInputStream(channel), StandardCharsets.UTF_8))) {
////                String line;
////                while ((line = br.readLine()) != null) {
////                    System.out.println(line);
////                    System.out.println("next line will be read from pos: " + coreChannel.position());
////                }
////                System.out.println(coreChannel.position());
////            }
////        }
//    }


    /** Snippet for checking the behavior of parallel streams w.r.t. item order. */
    public static void orderTest() {
        List<Integer> ids = IntStream.range(0, 10).boxed().toList();
        List<String> letters = IntStream.range((int)'a', (int)'j').mapToObj(x -> Character.toString((char)x)).toList();

        List<String> list = ids.parallelStream().flatMap(x -> letters.stream().map(y -> "" + x + "" + y))
            .toList();
            //.forEachOrdered(item -> System.out.println(item));

        System.out.println(list);

        if (true) { return; }
    }
}
