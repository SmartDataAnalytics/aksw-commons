package org.aksw.commons.io.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import com.google.common.collect.Lists;

public class FileUtils {

    /** Delete a path if it is an empty directory */
    public void deleteDirectoryIfEmpty(Path path) throws IOException {
        boolean isDirectory = Files.isDirectory(path);
        if (isDirectory) {
            boolean isEmpty = Files.list(path).count() == 0;
            if (isEmpty) {
                Files.delete(path);
            }
        }
    }

    /** TODO Add: Recursively delete all empty folders
     * @throws IOException */



    /**
     * Return a list of files matching a given glob pattern and a base path
     *
     * @param basePath
     * @param glob
     * @return
     * @throws IOException
     */
    public static List<Path> listPaths(Path basePath, String glob) throws IOException {
        List<Path> result = null;
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(basePath, glob)) {
            result = Lists.newArrayList(stream.iterator());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T readObject(Path target) throws IOException, ClassNotFoundException {
        Object obj;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(target, StandardOpenOption.READ))) {
            obj = in.readObject();
        }

        return (T)obj;
    }

    public static void writeObject(Path target, Object obj) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(target, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
            out.writeObject(obj);
            out.flush();
        }
    }

}
