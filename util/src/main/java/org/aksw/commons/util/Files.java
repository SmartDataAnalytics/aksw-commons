package org.aksw.commons.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class Files {
     private static final Logger logger = LoggerFactory.getLogger(Files.class);


    /**
     * like writefile, but always deletes any existing file
     * @param file
     * @param content
     * @throws IOException
     */
    public static void createFile(File file, String content) throws IOException{
        writeToFile(file, content, false);
    }

    public static void writeToFile(File file, String content, boolean append) throws IOException {
        FileWriter fw = new FileWriter(file, append);
        try {
            fw.write(content);
            fw.flush();
        } finally {
            fw.close();
        }

    }

    public static String readContent(File file) throws IOException {
        return URLHelper.readContent(file.toURI().toURL());
    }

    public static void mkdir(File path){
        if(!path.exists()){
            path.mkdirs();
            logger.info("created path: "+path);
        }
    }




}
