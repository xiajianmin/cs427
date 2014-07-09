package org.jenkinsci.plugins.tools_autodiscover.modules;

import hudson.model.JDK;
import hudson.tools.ToolInstallation;
import org.jenkinsci.plugins.tools_autodiscover.util.Environment;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JDKDiscoveryWindowsTest {

    private Environment mockEnvironment;
    private JDKDiscoveryWindows jdkWin;

    private boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }

    @Before
    public void setUp() {
        mockEnvironment = mock(Environment.class);
        when(mockEnvironment.isMacOSX()).thenReturn(false);
        when(mockEnvironment.isWindows()).thenReturn(true);
        jdkWin = new JDKDiscoveryWindows(mockEnvironment);
    }

    //Test if the implementation can actually find JDK installed on Windows. 
    @Test
    public void testFindJDKList() throws Exception {
        Assume.assumeTrue(isWindows());

        List<JDK> ls = jdkWin.findJava();
        assertFalse(ls.isEmpty());
    }

    /**
     * this test check the filter function
     * it creates dummy paths, and duplicate paths
     * only 2 of the paths are real, the duplicate is real
     * add them to list, and run filter function
     * there are 5 dummy paths input, expected 2 in result
     *
     * @throws Exception
     */
    @Test
    public void testFilteredJDKList() throws Exception {
        Assume.assumeTrue(isWindows());

        List<JDK> ls = new ArrayList<JDK>();
        JDK newJDK;

        newJDK = new JDK("JDK1", "C:/Program Files");
        ls.add(newJDK);
        ls.add(newJDK);
        newJDK = new JDK("JDK2", "C:/Windows");
        ls.add(newJDK);
        newJDK = new JDK("JDK3", "D:/JDK3");
        ls.add(newJDK);
        newJDK = new JDK("JDK4", "C:/Program File/Java/JDK4");
        ls.add(newJDK);

        List<JDK> filtered = jdkWin.filterJDKList(ls);

        assertEquals(2, filtered.size());
        for (JDK loc : filtered) {
            boolean content = (loc.getHome().equals("C:/Program Files") || loc.getHome().equals("C:/Windows"));
            assertTrue(content);
        }
    }

    /**
     * this test to get the version name from the path taken
     */
    @Test
    public void testGetJDKVersion() {
        Assume.assumeTrue(isWindows());

        String test = "C:\\JDK1\\1.7.0_25";
        assertEquals("1.7.0_25", jdkWin.getJDKVersion(test));

        test = "D:\\JDK2\\Java\\bin\\1.6.1_35";
        assertEquals("1.6.1_35", jdkWin.getJDKVersion(test));
    }

    //just make sure the code runs as expected. 
    @Test
    public void testGetToolInstallations() {
        Assume.assumeTrue(isWindows());

        assertTrue(jdkWin.isApplicable(JDK.class));
        List<ToolInstallation> ls = jdkWin.getToolInstallations(JDK.class);

        assertFalse(ls.isEmpty());
    }

    @Test
    public void testIsApplicableNull() {
        assertFalse(jdkWin.isApplicable(null));
    }

    @Test
    public void testIsApplicable() {
        assertTrue(jdkWin.isApplicable(JDK.class));
    }

    @Test
    public void testIsApplicableIncompatibleType() {
        assertFalse(jdkWin.isApplicable(ToolInstallation.class));
    }

    @Test
    public void testIsApplicableIncompatibleOS() {
        when(mockEnvironment.isWindows()).thenReturn(false);
        assertFalse(jdkWin.isApplicable(JDK.class));
    }
}
