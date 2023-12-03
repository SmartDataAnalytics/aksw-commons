package org.aksw.commons.io.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public class TestUriUtils {
    @Test
    public void test01() {
        Multimap<String, String> expected = ImmutableMultimap.<String, String>builder()
                .put("a", "b").put("c", "d").build();
        // The actual map is a LinkedHashMultiMap so the order should always be as given
        Multimap<String, String> actual = UriUtils.parseQueryStringAsMultimap("a=b&c=d");
        // The entry collections differ in type so we need array lists
        Assert.assertEquals(new ArrayList<>(expected.entries()), new ArrayList<>(actual.entries()));
    }

    /** null query string must result in empty list */
    @Test
    public void testNullQueryStringResultsInEmptyList() {
        Assert.assertEquals(List.of(), UriUtils.parseQueryStringAsList(null));
    }

    /** empty query string must result in empty list */
    @Test
    public void testEmptyQueryStringResultsInEmptyList() {
        Assert.assertEquals(List.of(), UriUtils.parseQueryStringAsList(""));
    }

    /** empty list result in null query string */
    @Test
    public void testEmptyListResultsInNullQueryString() {
        Assert.assertEquals(null, UriUtils.toQueryString(List.of()));
    }

}
