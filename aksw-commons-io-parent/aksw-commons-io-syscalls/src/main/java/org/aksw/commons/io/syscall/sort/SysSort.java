package org.aksw.commons.io.syscall.sort;

/** Simplifies parameter model for the /usr/bin/sort command */
public class SysSort {

//    @Option(names = { "-k", "--key" })
    public String key = null;

//    @Option(names = { "-R", "--random-sort" })
    public boolean randomSort = false;

//    @Option(names = { "-r", "--reverse" })
    public boolean reverse = false;

//    @Option(names = { "-u", "--unique" })
    public boolean unique = false;

//    @Option(names = { "-S", "--buffer-size" })
    public String bufferSize = null;

//    @Option(names = { "-T", "--temporary-directory" })
    public String temporaryDirectory = null;

//     TODO Integrate oshi to get physical core count by default
//    @Option(names = { "--parallel" })
    public int parallel = -1;

    // TODO Clarify merge semantics
    // At present it is for conflating consecutive named graphs with the same name
    // into a single graph
//    @Option(names = { "-m", "--merge" })
    public boolean merge = false;


}
