package org.aksw.commons.collections;

import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 8/12/11
 *         Time: 4:59 PM
 */
public class DescenderIteratorTest {
    @Test
    public void test1() throws InterruptedException {
        File base = new File("/home/raven/Projects/Current/Eclipse/LinkedData-QA/reports");

        // Scan the file-system recursively for reports.
        DescenderIterator<File> it = new DescenderIterator<File>(base, new FileDescender());



        while (it.hasNext()) {
            List<File> current = it.next();
                //System.out.println("Path: " + current);
                //Thread.sleep(1000l);

                //if(!isAccepted(current)) {
                /*
                if(true) {
                    it.next();
                    continue;
                }
                */

                if(!it.canDescend()) {
                    // We got a file
                    File f = current.get(current.size() - 1);
                    System.out.println(f);
                    if(f.getAbsolutePath().equals("/home/raven/Projects/Current/Eclipse/LinkedData-QA/reports/test/sampled/onlyout/dbpedia-linkedgeodata-city/Centrality.dat")) {
                        System.out.println("here");
                    }

                    //it.next();
                }
                else {
                    // We have not yet reached a leaf, therefore descend
                    it.descend();
                    //it.next();
                }
        }
    }
}
