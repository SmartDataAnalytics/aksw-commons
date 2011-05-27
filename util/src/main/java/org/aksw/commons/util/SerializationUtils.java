package org.aksw.commons.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import javax.xml.bind.JAXBException;
import java.io.*;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class SerializationUtils {

    public static void serializeXml(Object obj, File file)
            throws IOException {
        serializeXml(obj, file, false);
    }

    /**
     * @param obj
     * @param file
     * @param force Whether to attempt to create parent dirs
     * @throws JAXBException
     * @throws IOException
     */
    public static void serializeXml(Object obj, File file, boolean force)
            throws IOException {
        file.getParentFile().mkdirs();

        OutputStream out = new FileOutputStream(file);
        serializeXml(obj, out);
        out.flush();
        out.close();
    }

    public static void serializeXml(Object obj, OutputStream out) {
        XStream xstream = new XStream(new DomDriver());
        xstream.toXML(obj, out);
    }

    @SuppressWarnings("unchecked")
    public static Object deserializeXml(InputStream in)
            throws IOException {
        XStream xstream = new XStream(new DomDriver());
        Object result = xstream.fromXML(in);

        return result;
    }


    public static Object deserializeXml(File file)
            throws IOException {
        InputStream in = new FileInputStream(file);
        Object result = deserializeXml(in);

        in.close();
        return result;
    }
}
