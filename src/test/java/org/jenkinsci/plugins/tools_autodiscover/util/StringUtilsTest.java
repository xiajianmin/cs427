package org.jenkinsci.plugins.tools_autodiscover.util;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class StringUtilsTest {

    @Test
    public void testListToString() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("Hello");
        list.add(" ");
        list.add("World");
        assertTrue(StringUtils.listToString(list).equals("Hello World"));
    }
}
