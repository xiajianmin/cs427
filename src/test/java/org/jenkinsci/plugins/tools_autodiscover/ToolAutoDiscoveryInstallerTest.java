package org.jenkinsci.plugins.tools_autodiscover;

import hudson.model.JDK;
import hudson.tools.ToolInstallation;
import org.jenkinsci.plugins.tools_autodiscover.ToolAutoDiscoveryInstaller.DescriptorBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ToolAutoDiscoveryInstallerTest {
    @Test
    public void testGetToolInstallationsNoModule() {
        DescriptorBase installerDescriptor = new DescriptorBase(ToolInstallation.class);

        assertTrue(installerDescriptor.getToolInstallations().isEmpty());
    }

    @Test
    public void testGetToolInstallationsWrongModule() {
        ToolAutoDiscoveryModule mockModule = getJDKDiscoveryMock();
        DescriptorBase installerDescriptor = new DescriptorBase(JDK.class, mockModule);

        List<ToolInstallation> list = installerDescriptor.getToolInstallations(ToolInstallation.class);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testGetToolInstallationsDuplicates() {
        // create descriptor with two instances of the same module
        DescriptorBase installerDescriptor = new DescriptorBase(JDK.class,
                getJDKDiscoveryMock(), getJDKDiscoveryMock());

        List<ToolInstallation> list = installerDescriptor.getToolInstallations();
        assertEquals(1, list.size());
        assertEquals("TestJDK", list.get(0).getName());
        assertEquals("/", list.get(0).getHome());
    }

    @Test
    public void testIsApplicableNoModule() {
        DescriptorBase installerDescriptor = new DescriptorBase(ToolInstallation.class);

        assertFalse(installerDescriptor.isApplicable(ToolInstallation.class));
        assertFalse(installerDescriptor.isApplicable(JDK.class));
    }

    @Test
    public void testIsApplicable() {
        ToolAutoDiscoveryModule mockModule = getJDKDiscoveryMock();
        DescriptorBase installerDescriptor = new DescriptorBase(JDK.class, mockModule);

        assertFalse(installerDescriptor.isApplicable(ToolInstallation.class));
        assertTrue(installerDescriptor.isApplicable(JDK.class));
    }

    private ToolAutoDiscoveryModule getJDKDiscoveryMock() {
        ToolAutoDiscoveryModule mockModule = mock(ToolAutoDiscoveryModule.class);
        when(mockModule.isApplicable(JDK.class)).thenReturn(true);
        when(mockModule.getToolInstallations(JDK.class))
                .thenReturn(Arrays.asList((ToolInstallation) new JDK("TestJDK", "/")));
        return mockModule;
    }
}
