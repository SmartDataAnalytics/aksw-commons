package org.aksw.commons.path.core;

import org.junit.Assert;
import org.junit.Test;

public class TestPathStr {

    @Test
    public void testEscaping() {
        String str = "/hell\\//o/world/";
        Path<String> a = PathOpsStr.create(str);

        String aStr = a.toString();

        // System.out.println(aStr + " -> " + a.getSegments());

        Path<String> b = PathOpsStr.create(aStr);
        String bStr = b.toString();

        Assert.assertEquals(aStr, bStr);
    }
}
