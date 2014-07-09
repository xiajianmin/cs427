package org.jenkinsci.plugins.tools_autodiscover.modules;

import hudson.tasks.Maven.MavenInstallation;
import hudson.tools.ToolInstallation;
import org.jenkinsci.plugins.tools_autodiscover.util.Environment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.*;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class for Testing MavenDiscoveryWindows.java
 *
 * @author huq2 hlim10
 */
public class MavenDiscoveryWindowsTest {

    private Environment mockEnvironment;
    private MavenDiscoveryWindows mavenDisc;
    private final String MAVEN_VERSION_MESSAGE = "Apache maven 3.1.1 compiled on Jan 1 2012";
    private final String MAVEN_HOME_INCORRECT_MESSAGE = "";
    Process mockProcess = mock(Process.class);
    Process mockProcessIncorrect = mock(Process.class);

    /**
     * Setup for MavenDiscoveryWindowsTest.
     * <p/>
     * Sets up mock environment to and processes.
     *
     * @throws UnsupportedEncodingException
     */
    @Before
    public void setUp() throws UnsupportedEncodingException {
        mockEnvironment = mock(Environment.class);
        when(mockEnvironment.isMacOSX()).thenReturn(false);
        when(mockEnvironment.isWindows()).thenReturn(true);
        mavenDisc = new MavenDiscoveryWindows(mockEnvironment);

        mockProcess = mock(Process.class);
        mockProcessIncorrect = mock(Process.class);

        InputStream correctOutput =
                getStringInputStream(MAVEN_VERSION_MESSAGE, "UTF-8");
        InputStream incorrectOutput =
                getStringInputStream(MAVEN_HOME_INCORRECT_MESSAGE, "UTF-8");
        when(mockProcess.getInputStream()).thenReturn(correctOutput);
        when(mockProcessIncorrect.getInputStream()).thenReturn(incorrectOutput);

        when(mockEnvironment.getPath()).thenReturn("\\?\\maven1\\bin;\\?\\foo;\\?\\maven2\\bin");

        try {
            when(mockEnvironment.exec(any(String.class), any(String[].class), any(File.class)))
                    .thenAnswer(new Answer<Process>() {
                        public Process answer(InvocationOnMock invocation) throws Throwable {
                            Object[] args = invocation.getArguments();
                            String cmd = (String) args[0];
                            if (cmd == null) {
                                return null;
                            } else if (!cmd.toLowerCase().contains("maven") || !cmd.toLowerCase().contains("-version")) {
                                return null;
                            } else if (cmd.contains("\\?\\maven2\\bin")) {
                                return mockProcessIncorrect;
                            } else if (cmd.contains("\\?\\maven1\\bin")) {
                                return mockProcess;
                            }

                            return null;
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetMVNVersion() throws Exception {
        assertNull("Returned version of nonexistent maven", mavenDisc.getMVNVersion("\\?\\maven2\\bin"));
        assertNotNull("Returned null of existing maven", mavenDisc.getMVNVersion("\\?\\maven1\\bin"));
    }

    @Test
    public void testGetToolInstallations() throws Exception {
        List<ToolInstallation> installationList =
                mavenDisc.getToolInstallations(MavenInstallation.class);
        assertEquals(1, installationList.size());
        assertEquals("Apachemaven3.1.1", installationList.get(0).getName());
        assertEquals("\\?\\maven1", installationList.get(0).getHome());

    }

    private InputStream getStringInputStream(String str, String charsetName) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(str.getBytes(charsetName));
    }

}
