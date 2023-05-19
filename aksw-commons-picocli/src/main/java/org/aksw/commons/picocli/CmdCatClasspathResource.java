package org.aksw.commons.picocli;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.aksw.commons.io.util.StdIo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/** Command for printing out classpath resources */
@Command(name = "cpcat", description = "Print out the content of classpath resources")
public class CmdCatClasspathResource
    implements Callable<Integer>
{
    protected Logger logger = LoggerFactory.getLogger(CmdCatClasspathResource.class);

    @Parameters(arity = "1..n", description = "Classpath resource names")
    protected List<String> resourceNames = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Map<String, InputStream> map = new LinkedHashMap<>();

        try {
            List<String> failedNames = new ArrayList<>();
            for (String resourceName : resourceNames) {
                InputStream in = classLoader.getResourceAsStream(resourceName);
                if (in == null) {
                    failedNames.add(resourceName);
                } else {
                    map.put(resourceName, in);
                }
            }

            if (!failedNames.isEmpty()) {
                throw new FileNotFoundException("The following classpath resources could not be opened: " + failedNames);
            }

            try (OutputStream out = StdIo.openStdOut()) {
                for (Entry<String, InputStream> entry : map.entrySet()) {
                    entry.getValue().transferTo(out);
                }
            }
        } finally {
            for (Entry<String, InputStream> entry : map.entrySet()) {
                try {
                    entry.getValue().close();
                } catch (Exception e) {
                    logger.warn("Failed to close resource " + entry.getKey(), e);
                }
            }
        }

        return 0;
    }
}
