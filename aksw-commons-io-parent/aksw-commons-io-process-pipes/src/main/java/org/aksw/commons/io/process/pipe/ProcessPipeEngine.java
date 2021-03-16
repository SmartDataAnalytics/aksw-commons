package org.aksw.commons.io.process.pipe;

import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.commons.io.endpoint.FileCreation;

/**
 * Interface to perform process execution.
 * The purpose is to provide an abstraction that allows for additional implementations besides
 * {@link ProcessPipeEngineNative}.
 * One such future implementation may be based on the NuProcess library.
 *
 * @author raven
 *
 */
public interface ProcessPipeEngine {
    BiFunction<Path, Path, FileCreation> mapPathToPath(BiFunction<Path, Path, String[]> cmdBuilder);
    PathToStream mapPathToStream(Function<Path, String[]> cmdBuilder);
    StreamToStream mapStreamToStream(String[] cmd);
    BiFunction<InputStreamOrPath, Path, FileCreation> mapStreamToPath(Function<Path, String[]> cmdBuilder);
}
