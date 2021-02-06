package org.aksw.commons.io.process.pipe;

import java.util.ArrayList;
import java.util.List;

public class SysCallPipeBuilder {
    protected List<SysCallPipeSpec> specs;

    public SysCallPipeBuilder(List<SysCallPipeSpec> specs) {
        this.specs = specs;
    }

    public SysCallPipeBuilder create() {
        return new SysCallPipeBuilder(new ArrayList<>());
    }

    public SysCallPipeBuilder add(SysCallPipeSpec spec) {
        specs.add(spec);
        return this;
    }


    public SysCallPipeSpec build() {
        // PipeTransformSysCallStream

        throw new RuntimeException("Not implemented");
    }

}
