package org.aksw.commons.picocli;

import java.io.Serializable;

import picocli.CommandLine.Option;

public class CmdMixinLogLevel
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Option(names = { "--log-level" }, description="Log level (of the root logger)")
    public String logLevel = null;
}
