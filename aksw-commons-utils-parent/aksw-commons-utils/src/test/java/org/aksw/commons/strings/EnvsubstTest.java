package org.aksw.commons.strings;

import org.aksw.commons.util.string.Envsubst;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;


public class EnvsubstTest {

    @Test
    public void test1() {
        String actual = Envsubst.envsubst("$bar ${foo}? $foo ${bar}!", ImmutableMap.<String, String>builder()
                .put("foo", "hello")
                .put("bar", "world")
                .build()::get);

        Assert.assertEquals("world hello? hello world!", actual);
    }
}
