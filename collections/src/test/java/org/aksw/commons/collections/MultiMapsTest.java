package org.aksw.commons.collections;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by Claus Stadler
 * Date: 2/27/11
 * Time: 12:39 PM
 */
public class MultiMapsTest {
    private static Logger logger = LoggerFactory.getLogger(MultiMapsTest.class);

    @Test
    public void testTransitivity()
    {
        int randomSeed = 0;
        int numIterations = 10;
        int numNodes = 20;
        int numEdges = numNodes * numNodes / 4;

        Random random = new Random();

        for(int i = 0; i < numIterations; ++i) {
            Map<Integer, Set<Integer>> mapA = new HashMap<Integer, Set<Integer>>();

            for(int j = 0; j < numEdges; ++j) {
                Integer from = random.nextInt(numNodes);
                Integer to = random.nextInt(numNodes);

                MultiMaps.put(mapA, from, to);
            }

            Map<Integer, Set<Integer>> mapB = MultiMaps.transitiveClosure(mapA);

            logger.trace("");
            logger.trace("Iteration " + i + "/" + numIterations);
            logger.trace("Map: " + mapA);

            //System.out.println("Begin Iteration " + i + "/" + numIterations);

            for(Integer node : mapA.keySet()) {
                Set<Integer> a = MultiMaps.transitiveGet(mapA, node);
                Set<Integer> b = MultiMaps.safeGet(mapB, node);

                logger.trace("TGet: " + a);
                logger.trace("TMap: " + b);

                Assert.assertEquals(a, b);
            }
            //System.out.println("End of Iteration " + i + "/" + numIterations);

            logger.trace("");
        }
    }
}
