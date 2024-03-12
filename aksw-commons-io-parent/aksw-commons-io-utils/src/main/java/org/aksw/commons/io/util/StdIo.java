package org.aksw.commons.io.util;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;


/**
 * A utility class to open (close-shielded) input/output streams
 * from stdout, stderr and stdin.
 *
 * @author raven
 *
 */
public class StdIo {
    public static OutputStream openStdOut() {
        return new FileOutputStream(FileDescriptor.out);
    }

    public static OutputStream openStdErr() {
        return new FileOutputStream(FileDescriptor.err);
    }

    public static InputStream openStdIn() {
        return new FileInputStream(FileDescriptor.in);
    }

    public static OutputStream openStdOutWithCloseShield() {
        return CloseShieldOutputStream.wrap(openStdOut());
    }

    public static OutputStream openStdErrWithCloseShield() {
        return CloseShieldOutputStream.wrap(openStdErr());
    }

    public static InputStream openStdInWithCloseShield() {
        return CloseShieldInputStream.wrap(openStdIn());
    }
}
