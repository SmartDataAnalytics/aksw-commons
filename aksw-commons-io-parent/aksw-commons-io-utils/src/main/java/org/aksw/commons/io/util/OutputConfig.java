package org.aksw.commons.io.util;

public interface OutputConfig {
    String getOutputFormat();
    String getTargetFile();
    boolean isOverwriteAllowed();
}
