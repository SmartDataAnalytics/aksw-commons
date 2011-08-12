package org.aksw.commons.strings;

import org.aksw.commons.util.strings.BifContains;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class TestBifContains {

    @Test
    public void test(){
        String test =  "\"this\" and \"is\" and \"a\" and \"test\"";
        BifContains b = new BifContains("this is a      test");
        System.out.println(b.makeWithAnd());
        Assert.assertTrue(b.makeWithAnd().equals(test));

    }

}
