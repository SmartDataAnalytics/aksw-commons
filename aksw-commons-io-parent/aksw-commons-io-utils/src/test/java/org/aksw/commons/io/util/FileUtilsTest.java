package org.aksw.commons.io.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.commons.io.util.FileUtils.OverwritePolicy;
import org.junit.Assert;
import org.junit.Test;


public class FileUtilsTest {
    public static final String TEST_STRING = "Hello";

    @Test
    public void test_safeCreate_00() throws Exception {
        Path path = Files.createTempFile("test", ".tmp");
        writeTestFileAndAssert(path, OverwritePolicy.SKIP, TEST_STRING, "");
    }

    @Test
    public void test_safeCreate_01() throws Exception {
        Path path = Files.createTempFile("test", ".tmp");
        writeTestFileAndAssert(path, OverwritePolicy.OVERWRITE);
    }

    @Test
    public void test_safeCreate_02() throws Exception {
        Path path = Files.createTempFile("test", ".tmp");
        writeTestFileAndAssert(path, OverwritePolicy.OVERWRITE_ALWAYS);
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void test_safeCreate_03() throws Exception {
        Path path = Files.createTempFile("test", ".tmp");
        writeTestFileAndAssert(path, OverwritePolicy.ERROR);
    }

    public void writeTestFileAndAssert(Path path, OverwritePolicy overwritePolicy, String writeStr, String assertStr) throws Exception {
        try {
            FileUtils.safeCreate(path, overwritePolicy, out -> out.write(writeStr.getBytes(StandardCharsets.UTF_8)));
            assertFileContent(path, assertStr);
        } finally {
            Files.deleteIfExists(path);
        }
    }

    public void writeTestFileAndAssert(Path path, OverwritePolicy overwritePolicy) throws Exception {
        try {
            FileUtils.safeCreate(path, overwritePolicy, out -> out.write(TEST_STRING.getBytes(StandardCharsets.UTF_8)));
            assertFileContent(path, TEST_STRING);
        } finally {
            Files.deleteIfExists(path);
        }
    }

    public static void assertFileContent(Path path, String expected) {
        String actual;
        try {
            actual = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assert.assertEquals(expected, actual);
    }
}
