package org.jenkinsci.plugins.tools_autodiscover.modules;

import hudson.tasks.Maven;
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

public class MavenDiscoveryOSXTest {

    public static final String MVN_VERSION_MESSAGE = "Apache Maven 2.2.1 (r801777; 2009-08-06 14:16:01-0500)\n Java version: 1.6.0_65\n Java home: /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home\nDefault locale: en_US, platform encoding: MacRoman\nOS name: \"mac os x\" version: \"10.9\" arch: \"x86_64\" Family: \"mac\"";
    public static final String EXPECTED_OUTPUT = "Apache Maven 2.2.1";
    public static final String EXPECTED_PATH = "/opt/local/share/java/maven2/bin/mvn";
    public static final int EXPECTED_NUM_ELEMENTS = 1;

    private Environment mockEnvironment;
    private MavenDiscoveryOSX module;


    @Before
    public void setUp() {
        // Set up expected file list
        final File mockFileAnt = mock(File.class);
        final File mockFileMvn2 = mock(File.class);

        when(mockFileAnt.getName()).thenReturn("apache-ant");
        when(mockFileMvn2.getName()).thenReturn("maven2");

        File[] expectedFileList = new File[2];
        expectedFileList[0] = mockFileAnt;
        expectedFileList[1] = mockFileMvn2;

        final File mockInstallationDir = mock(File.class);

        when(mockInstallationDir.listFiles()).thenReturn(expectedFileList);
        
        mockEnvironment = mock(Environment.class);
        when(mockEnvironment.isMacOSX()).thenReturn(true);
        when(mockEnvironment.isWindows()).thenReturn(false);
        module = new MavenDiscoveryOSX(mockEnvironment, mockInstallationDir);
    }


    @Test
    public void testExtractMavenVersionInfo() {
        String computedOutput = module.extractMavenVersionInfo(MVN_VERSION_MESSAGE);
        assertTrue(computedOutput.equals(EXPECTED_OUTPUT));
    }

    @Test
    public void testIsApplicableNull() {
        assertFalse(module.isApplicable(null));
    }

    @Test
    public void testIsApplicable() {
        assertTrue(module.isApplicable(Maven.MavenInstallation.class));
    }

    @Test
    public void testIsApplicableIncompatibleType() {
        assertFalse(module.isApplicable(ToolInstallation.class));
    }

    @Test
    public void testIsApplicableIncompatibleOS() {
        when(mockEnvironment.isMacOSX()).thenReturn(false);
        assertFalse(module.isApplicable(Maven.MavenInstallation.class));
    }

    @Test
    public void testIsApplicableCompatibleOS() {
        when(mockEnvironment.isMacOSX()).thenReturn(true);
        assertTrue(module.isApplicable(Maven.MavenInstallation.class));
    }

    @Test
    public void testGetToolInstallation() throws Exception {
        final Process mockProcess = mock(Process.class);

        InputStream expectedVersionOutput = getStringInputStream(MVN_VERSION_MESSAGE, "UTF-8");
        when(mockProcess.getInputStream()).thenReturn(expectedVersionOutput);
        when(mockEnvironment.exec(any(String.class), any(String[].class), any(File.class))).thenReturn(mockProcess);
        List<ToolInstallation> installationList = module.getToolInstallations(Maven.MavenInstallation.class);
        assertEquals(EXPECTED_NUM_ELEMENTS, installationList.size());
        assertEquals(EXPECTED_OUTPUT, installationList.get(0).getName());
        assertEquals(EXPECTED_PATH, installationList.get(0).getHome());

    }


    private InputStream getStringInputStream(String str, String charsetName) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(str.getBytes(charsetName));
    }


}
