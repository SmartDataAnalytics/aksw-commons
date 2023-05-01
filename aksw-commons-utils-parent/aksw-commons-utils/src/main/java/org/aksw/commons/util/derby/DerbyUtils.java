package org.aksw.commons.util.derby;

import java.io.OutputStream;

/**
 * Utils to disable derby.log file - see: https://stackoverflow.com/questions/1004327/getting-rid-of-derby-log
 * Derby is occasionally pulled in by certain dependencies.
 *
 * System.setProperty("derby.stream.error.field", "org.aksw.commons.util.derby.DerbyUtils.DEV_NULL");
 *
 */
public class DerbyUtils {
    public static final OutputStream DEV_NULL = new OutputStream() {
        public void write(int b) {}
    };

    public static void disableDerbyLog() {
        System.setProperty("derby.stream.error.field", "org.aksw.commons.util.derby.DerbyUtils.DEV_NULL");
    }
}
