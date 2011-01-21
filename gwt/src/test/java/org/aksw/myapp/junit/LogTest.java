package org.aksw.myapp.junit;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 * Time:  23.09.2010 02:45:19
 */
public class LogTest {
private static final Logger logger = Logger.getLogger(LogTest.class);
    @Test
    public void test(){
         logger.error("test");
    }
}
