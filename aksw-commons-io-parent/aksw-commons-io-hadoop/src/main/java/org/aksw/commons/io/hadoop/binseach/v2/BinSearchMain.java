package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import org.aksw.commons.io.binseach.BinarySearcher;
import org.apache.hadoop.io.compress.BZip2Codec;

public class BinSearchMain {
    public static void main(String[] args) throws IOException {
        // Path path = Path.of("/home/raven/Projects/Eclipse/linkedgeodata-parent/legacy/linkedgeodata-dump/target/dump/2018-04-04/2018-04-04-Amenity.node.sorted.nt.bz2");

        // System.out.println((Byte.compare((byte)'>', (byte)'9')));

        Path path = Path.of("/media/raven/T9/raven/datasets/wikidata/2024-08-24_wikidata-truthy.sorted.nt.bz2");

        BinarySearcher binSearcher = BinarySearchBuilder.newBuilder()
                .setSource(path)
                .setCodec(new BZip2Codec())
                .build();

        byte[] allPrefix = "<http://linkedgeodata.org/".getBytes();
        byte[] prefix = "<http://linkedgeodata.org/geometry/node1202810066>".getBytes(); // 1

        byte[] first = "<http://linkedgeodata.org/geometry/node1000036734>".getBytes();  // 0
        byte[] last = "<http://linkedgeodata.org/triplify/node999596437>".getBytes();    // 18807004


        byte[] wd = "<http://www.wikidata.org/entity/Q24075>".getBytes();

        byte[] lookup = wd;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(binSearcher.search(lookup)))) {
            br.lines().forEach(x -> System.out.println(x));
        }
    }
}
