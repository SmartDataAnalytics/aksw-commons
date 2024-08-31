package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.aksw.commons.io.input.SeekableReadableChannel;
import org.apache.hadoop.io.compress.BZip2Codec;

public class BinSearchLookup {
    public static void main(String[] args) throws IOException {

        Path path = Paths.get("/media/raven/T9/raven/datasets/wikidata/2024-08-24_wikidata-truthy.sorted.nt.bz2");

        BlockSource blockSource = BlockSource.of(path, new BZip2Codec());

        byte[] data = new byte[10];
        // e -> 44580
        //t -> 44716

        try (SeekableReadableChannel<byte[]> channel = blockSource.newReadableChannel(44500)) {

            int n = channel.read(data, 0, data.length);
            System.out.println(new String(data, StandardCharsets.UTF_8));
            System.out.println("pos: " + channel.position());
        }


            // lines.limit(10000).forEach(x -> System.out.println("Line: " + x));

           //  System.out.println("Count: " + lines.count());
    }
}
