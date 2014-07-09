package org.jenkinsci.plugins.tools_autodiscover.modules;

import hudson.tasks.Ant;
import hudson.tools.ToolInstallation;
import org.jenkinsci.plugins.tools_autodiscover.util.Environment;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AntDiscoveryOSXTest {

    public static final String ANT_VERSION_MESSAGE = "Apache Ant(TM) version 1.3.5 compiled on Jan 1 2011";
    public static final String EXPECTED_OUTPUT = "Apache Ant 1.3.5";
    public static final String EXPECTED_PATH = "/opt/local/share/java/apache-ant/bin/ant";
    public static final int EXPECTED_NUM_ELEMENTS = 1;

    private Environment mockEnvironment;
    private AntDiscoveryOSX module;

    @Before
    public void setUp() {
        // Set up expected file list
        final File mockFileAnt = mock(File.class);
        final File mockFileMvn2 = mock(File.class);
        final File mockFileMvn3 = mock(File.class);

        when(mockFileAnt.getName()).thenReturn("apache-ant");
        when(mockFileMvn2.getName()).thenReturn("maven2");
        when(mockFileMvn3.getName()).thenReturn("maven3");

        File[] expectedFileList = new File[3];
        expectedFileList[0] = mockFileAnt;
        expectedFileList[1] = mockFileMvn2;
        expectedFileList[2] = mockFileMvn3;

        final File mockInstallationDir = mock(File.class);

        when(mockInstallationDir.listFiles()).thenReturn(expectedFileList);

        mockEnvironment = mock(Environment.class);
        when(mockEnvironment.isMacOSX()).thenReturn(true);
        when(mockEnvironment.isWindows()).thenReturn(false);
        module = new AntDiscoveryOSX(mockEnvironment, mockInstallationDir);
    }

    @Test
    public void testExtractAntVersionInfo() {
        String computedOutput = module.extractAntVersionInfo(ANT_VERSION_MESSAGE);
        assertTrue(computedOutput.equals(EXPECTED_OUTPUT));
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
        when(mockEnvironment.isMacOSX()).thenReturn(false);
        assertFalse(module.isApplicable(Ant.AntInstallation.class));
    }

    @Test
    public void testIsApplicableCompatibleOS() {
        when(mockEnvironment.isMacOSX()).thenReturn(true);
        assertTrue(module.isApplicable(Ant.AntInstallation.class));
    }

    @Test
    public void testGetToolInstallation() throws Exception {
        final Process mockProcess = mock(Process.class);

        InputStream expectedVersionOutput = getStringInputStream(ANT_VERSION_MESSAGE, "UTF-8");
        when(mockProcess.getInputStream()).thenReturn(expectedVersionOutput);
        when(mockEnvironment.exec(any(String.class), any(String[].class), any(File.class))).thenReturn(mockProcess);
        List<ToolInstallation> installationList = module.getToolInstallations(Ant.AntInstallation.class);
        assertEquals(EXPECTED_NUM_ELEMENTS, installationList.size());
        assertEquals(EXPECTED_OUTPUT, installationList.get(0).getName());
        assertEquals(EXPECTED_PATH, installationList.get(0).getHome());
    }


    private InputStream getStringInputStream(String str, String charsetName) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(str.getBytes(charsetName));
    }
}
