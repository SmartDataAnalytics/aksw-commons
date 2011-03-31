package org.aksw.commons.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Another File Helper implementation juhu
 * allows some additional read write options
 * User: Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class ExtendedFile {
      private static final Logger logger = Logger.getLogger(ExtendedFile.class);

    public boolean trim = true;
    final private File file;

    public ExtendedFile(File file) {
       this.file = file;
       logger.trace("File exists? "+file.exists());
    }

    public String readContentLogException(){
        try {
            return readContent();
        } catch (FileNotFoundException e) {
            logger.error("File could not be found "+file.toString(),e);
        }
        return "null";
    }

    public String readContent()throws FileNotFoundException{
        StringBuffer buf = new StringBuffer();
        Scanner scanner = new Scanner(file);
        try {
            // first use a Scanner to get each line
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                line = readOperations(line);
                line +=  "\n";
                buf.append(line);
            }
        } finally {
            // ensure the underlying stream is always closed
            scanner.close();
        }
        return buf.toString();
    }


     public List<String> readAsListLogException(){
        try {
            return readAsList();
        } catch (FileNotFoundException e) {
            logger.error("File could not be found "+file.toString(),e);
        }
        return new ArrayList<String>();
    }

    public List<String> readAsList()throws FileNotFoundException{
        List<String> list = new ArrayList<String>();
        Scanner scanner = new Scanner(file);

        try {
            // first use a Scanner to get each line
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                line = readOperations(line);
                list.add(line);
            }
        } finally {
            // ensure the underlying stream is always closed
            scanner.close();
        }
        return list;
    }

    private String readOperations(String line){
        line = (trim)?line.trim():line;
        return line;
    }

    public void writeFile(String content) throws IOException {
		FileWriter fw = new FileWriter(file, false);
		fw.write(content);
		fw.flush();
	}

}
