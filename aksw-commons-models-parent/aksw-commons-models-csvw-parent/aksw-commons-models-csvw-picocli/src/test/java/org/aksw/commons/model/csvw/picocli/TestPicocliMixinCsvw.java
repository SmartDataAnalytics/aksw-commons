package org.aksw.commons.model.csvw.picocli;

import org.aksw.commons.model.csvw.domain.api.DialectMutable;
import org.aksw.commons.model.csvw.domain.impl.DialectMutableImpl;
import org.junit.Assert;
import org.junit.Test;

import picocli.CommandLine;

public class TestPicocliMixinCsvw {

    /** Test whether passing cli arguments to the delegate via picocli works */
    @Test
    public void test1() {
        String encoding = "ISO-8859-1";
        DialectMutable state = new DialectMutableImpl();
        new CommandLine(PicocliMixinCsvw.of(state)).execute("-e", encoding);
        Assert.assertEquals(encoding, state.getEncoding());
    }
}
