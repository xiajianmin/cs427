package org.jenkinsci.plugins.tools_autodiscover.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests for PathUtils class
 */
public class PathUtilsTest {
    @Test
    public void testGetParentDirectoryWindows() {
        String parDirectory = "parent\\child";
        String result = PathUtils.Windows.getParentDir(parDirectory);
        assertEquals("parent", result);
        assertNotEquals("child", result);
    }
}
