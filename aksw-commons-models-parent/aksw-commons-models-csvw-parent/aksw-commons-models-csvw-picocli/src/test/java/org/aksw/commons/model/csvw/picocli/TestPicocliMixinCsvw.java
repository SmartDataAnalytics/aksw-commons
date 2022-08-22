package org.aksw.commons.model.csvw.picocli;

import org.aksw.commons.model.csvw.domain.api.DialectMutable;
import org.aksw.commons.model.csvw.domain.impl.DialectMutableImpl;
import org.junit.Assert;
import org.junit.Test;

import picocli.CommandLine;
import picocli.CommandLine.Mixin;

public class TestPicocliMixinCsvw {

    public static class MyTestCommand
        implements Runnable
    {
        @Mixin
        PicocliMixinCsvw csvw;

        @Override
        public void run() {
        }
    }

    /** Test whether passing cli arguments to the delegate via picocli works */
    @Test
    public void test1() {
        String encoding = "ISO-8859-1";
        MyTestCommand cmd = new MyTestCommand();
        DialectMutable state = new DialectMutableImpl();
        cmd.csvw = PicocliMixinCsvw.of(state);
        new CommandLine(cmd).execute("-e", encoding);
        Assert.assertEquals(encoding, state.getEncoding());
    }
}
