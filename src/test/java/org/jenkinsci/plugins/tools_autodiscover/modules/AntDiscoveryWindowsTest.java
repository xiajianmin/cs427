package org.jenkinsci.plugins.tools_autodiscover.modules;

import hudson.tasks.Ant;
import hudson.tools.ToolInstallation;
import org.jenkinsci.plugins.tools_autodiscover.util.Environment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class AntDiscoveryWindowsTest {

    private static final String ANT_HOME_INCORRECT_MESSAGE = "ANT_HOME is set incorrectly or ant could not be located. Please set ANT_HOME.";
    private static final String ANT_VERSION_MESSAGE = "Apache Ant(TM) version 1.3.5 compiled on Jan 1 2011";

    private Environment mockEnvironment;
    private AntDiscoveryWindows module;

    @Before
    public void setUp() {
        mockEnvironment = mock(Environment.class);
        when(mockEnvironment.isMacOSX()).thenReturn(false);
        when(mockEnvironment.isWindows()).thenReturn(true);
        module = new AntDiscoveryWindows(mockEnvironment);
    }

    @Test
    public void testParsePath() {
        List<String> pathEntries = module.parsePath(";who;is;this;");
        List<String> expectedEntries = Arrays.asList("who", "is", "this");
        assertEquals(expectedEntries, pathEntries);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParsePathNull() {
        module.parsePath(null);
    }

    @Test
    public void testParseVersion() {
        String version = module.parseVersion(ANT_VERSION_MESSAGE);
        assertEquals("1.3.5", version);
    }

    @Test
    public void testParseVersionIncorrect() {
        String version = module.parseVersion(ANT_HOME_INCORRECT_MESSAGE);
        assertNull(version); // returns null if could not be parsed
    }

    @Test
    public void testGetToolInstallations() throws Exception {
        final Process mockProcess = mock(Process.class);
        final Process mockProcessIncorrect = mock(Process.class);

        InputStream correctOutput =
                getStringInputStream(ANT_VERSION_MESSAGE, "UTF-8");
        InputStream incorrectOutput =
                getStringInputStream(ANT_HOME_INCORRECT_MESSAGE, "UTF-8");
        when(mockProcess.getInputStream()).thenReturn(correctOutput);
        when(mockProcessIncorrect.getInputStream()).thenReturn(incorrectOutput);

        when(mockEnvironment.getPath()).thenReturn("\\?\\TestDir\\bin;\\?\\foo;\\?\\TestDir2\\bin");
        when(mockEnvironment.exec(any(String.class), any(String[].class), any(File.class)))
                .thenAnswer(new Answer<Process>() {
                    public Process answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        String cmd = (String) args[0];

                        if (cmd == null) {
                            return null;
                        } else if (!cmd.toLowerCase().contains("ant") || !cmd.toLowerCase().contains("-version")) {
                            return null;
                        } else if (cmd.contains("\\?\\TestDir2\\bin")) {
                            return mockProcessIncorrect;
                        } else if (cmd.contains("\\?\\TestDir\\bin")) {
                            return mockProcess;
                        }

                        return null;
                    }
                });


        List<ToolInstallation> installationList =
                module.getToolInstallations(Ant.AntInstallation.class);
        assertEquals(1, installationList.size());
        assertEquals("Ant 1.3.5", installationList.get(0).getName());
        assertEquals("\\?\\TestDir", installationList.get(0).getHome());

    }

    private InputStream getStringInputStream(String str, String charsetName) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(str.getBytes(charsetName));
    }

    @Test
    public void testIsApplicableNull() {
        assertFalse(module.isApplicable(null));
    }

    @Test
    public void testIsApplicable() {
        assertTrue(module.isApplicable(Ant.AntInstallation.class));
    }

    @Test
    public void testIsApplicableIncompatibleType() {
        assertFalse(module.isApplicable(ToolInstallation.class));
    }

    @Test
    public void testIsApplicableIncompatibleOS() {
        when(mockEnvironment.isWindows()).thenReturn(false);
        assertFalse(module.isApplicable(Ant.AntInstallation.class));
    }
}
