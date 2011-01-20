package org.aksw.myapp.gwt;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 * Time:  23.09.2010 01:02:58
 */
public class FileTest {
 private static final Logger logger = Logger.getLogger(FileTest.class);
    Resource file;

    public static void main(String[] args) {

        System.out.println(System.getProperties());
        Properties p = System.getProperties();
Enumeration keys = p.keys();
while (keys.hasMoreElements()) {
  String key = (String)keys.nextElement();
  String value = (String)p.get(key);
  System.out.println(key + ": " + value);
}

        System.exit(0);
        ApplicationContext c = new ClassPathXmlApplicationContext("spring.xml");
        FileTest ft = (FileTest) c.getBean("fileTest");
        System.out.println(ft.toString());
        FileTest ft2 = (FileTest) c.getBean("fileTest2");
        System.out.println(ft2.toString());
    }
    @Override
    public String toString() {
        String message = "FileTest{" + "file=" + file + '}';
        try {
            message += "\ngetFilename(): " + getFile().getFilename();
            message += "\nexists(): " + getFile().exists();
            message += "\n.getFile().getAbsolutePath(): " + getFile().getFile().getAbsolutePath();
        } catch (IOException e) {
            logger.error("",e);
        }
        return message;
    }

    public void setFile(Resource file) {
        this.file = file;
    }

    public Resource getFile() {
        return file;
    }
}
