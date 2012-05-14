package org.aksw.commons.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class URLHelper {
    private static final Logger logger = LoggerFactory.getLogger(ExtendedFile.class);

    public static String readContentLogException(URL url) {
        try {
            return readContent(url);
        } catch (IOException e) {
            logger.error("URL could not be read " + url.toString(), e);
        }
        return "null";
    }

    public static String readContent(URL url) throws IOException {
        StringBuffer buf = new StringBuffer();
        Scanner scanner = new Scanner(url.openStream());
        try {
            // first use a Scanner to get each line
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                buf.append(line);
                buf.append("\n");
            }
        } finally {
            // ensure the underlying stream is always closed
            scanner.close();
        }
        return buf.toString();
    }

}
