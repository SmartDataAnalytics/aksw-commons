package org.aksw.commons.io.process.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.commons.io.endpoint.FileCreation;
import org.aksw.commons.util.exception.ExceptionUtilsAksw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;



/**
 * Utility functions to create file and input stream transform based on system calls
 *
 * InputStreams <b>passed as arguments</b> to the various processors are considered to be owned by the
 * processors. Hence, the processors are in charge of closing them.
 * The client code is in charge of closing InputStreams returned by the processors.
 *
 * TODO Enhance the FileCreation objects such that they expose a future for when
 * the process started modifying the file.
 *
 * @author raven
 *
 */
public class ProcessPipeUtils {
	private static final Logger logger = LoggerFactory.getLogger(ProcessPipeUtils.class); 
	


    /**
     * TODO Move this method elsewhere as it does not make use of a process
     *
     *
     * Transformation using a piped input/outputstream
     * createPipedTransform((in, out) -> { for(item : () -> readItems(in)) { write(item); } )
     *
     *
     * @param action
     * @return
     */
    public static Function<InputStream, InputStream> createPipedTransformer(BiConsumer<InputStream, OutputStream> action) {
        return in -> {
            PipedOutputStream pout = new PipedOutputStream();
            PipedInputStream pin;
            try {
                pin = new PipedInputStream(pout);
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            new Thread(() -> {
                try(OutputStream tmpOut = pout) {
                    action.accept(in, tmpOut);
                } catch (Exception e) {
                    ExceptionUtilsAksw.rethrowUnlessRootCauseMatches(e,
                            match -> {
                                /* Silently ignore, because closing the channel is valid */
                                // TODO Add logger just in case
                                logger.debug("Channel closed prematurely");
                            },
                            ExceptionUtilsAksw::isClosedChannelException);

                }
            }).start();
            return pin;
        };
    }

    /**
     * Create a new thread to copy from source to target
     * TODO Closing the target silently terminate the thread and associated
     * resources (such as a system process)
     *
     * Closing the input stream should not happen though as it is
     * considered to be owned by the copy process
     *
     * @param from
     * @param to
     * @return
     */
    public static Thread startThreadedCopy(InputStream from, OutputStream to, Consumer<Exception> failureCallback) {
        Thread result = new Thread(() -> {
            try(InputStream in = from; OutputStream out = to) {
                ByteStreams.copy(in, out);
            } catch (IOException e) {
                failureCallback.accept(e);
                throw new RuntimeException(e);
            }
        });

        result.start();
        return result;
    }


    public static Process startProcess(ProcessBuilder processBuilder) {
        Process result;
        try {
            result = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static PathToStream mapPathToStream(Function<Path, String[]> cmdBuilder) {
        return path -> {
            String[] cmd = cmdBuilder.apply(path);
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            ProcessSink r = new ProcessSinkImpl(processBuilder, p -> {});
            return r;
        };
    }

    public static FileCreation createFileCreation(Process process, Path path) {
        CompletableFuture<Path> future = new CompletableFuture<>();
        Thread thread = new Thread(() -> {
            try {
                process.waitFor();
                int exitValue = process.exitValue();
                if(exitValue == 0) {
                    future.complete(path);
                } else {
                    future.completeExceptionally(new RuntimeException("Process creating file " + path + " ended with non-zero exit code " + exitValue));
                }

            } catch(InterruptedException e) {
                // If this thread for whatever reason dies, try to kill the process
                process.destroy();
                future.completeExceptionally(e);
            } catch(Exception e) {
                future.completeExceptionally(e);
            }
        });
        thread.start();

        FileCreation r = new FileCreation() {
            @Override
            public CompletableFuture<Path> future() {
                return future;
            }

            @Override
            public void abort() throws Exception {
                process.destroy();
            }
        };

        return r;
    }

    public static BiFunction<InputStreamOrPath, Path, FileCreation> mapStreamToPath(Function<Path, String[]> cmdBuilder) {
        return (src, tgt) -> {
            String[] cmd = cmdBuilder.apply(tgt);

            ProcessBuilder processBuilder = new ProcessBuilder(cmd);

            if(src.isPath()) {
                processBuilder.redirectInput(src.getPath().toFile());
            }

            Process process;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            OutputStream out = process.getOutputStream();

            if(!src.isPath()) {
                // If the process dies, the copy thread dies anyway
                // But if the copy thread dies, the process may still be alive
                // Hence destroy the process on copy thread death
                startThreadedCopy(src.getInputStream(), out, e -> process.destroy());
            }

            FileCreation r = createFileCreation(process, tgt);
            return r;
        };
    }

    public static BiFunction<Path, Path, FileCreation> mapPathToPath(BiFunction<Path, Path, String[]> cmdBuilder) {
        return (src, tgt) -> {
            String[] cmd = cmdBuilder.apply(src, tgt);

            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            Process process;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            FileCreation r = createFileCreation(process, tgt);
            return r;
        };
    }

    public static StreamToStream mapStreamToStream(String[] cmd) {
        return src -> {
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            if(src.isPath()) {
                processBuilder.redirectInput(src.getPath().toFile());
            }

            ProcessSink r = new ProcessSinkImpl(processBuilder, p -> {
                if(!src.isPath()) {
                    OutputStream out = p.getOutputStream();
                    startThreadedCopy(src.getInputStream(), out, e -> p.destroy());
                }
            });

            return r;
        };
    }

    // TODO Variants that use redirects
    // Streaming variants may in addition redirect file to process
    // So we need a builder object in between

}

