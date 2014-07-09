package org.jenkinsci.plugins.tools_autodiscover;

import hudson.Extension;
import hudson.tasks.Maven;
import org.jenkinsci.plugins.tools_autodiscover.modules.MavenDiscoveryOSX;
import org.jenkinsci.plugins.tools_autodiscover.modules.MavenDiscoveryWindows;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Automatic discovery installer for Maven.
 */
@SuppressWarnings("unchecked")
public class MavenAutoDiscoveryInstaller extends ToolAutoDiscoveryInstaller {
    @DataBoundConstructor
    public MavenAutoDiscoveryInstaller(String toolHome) {
        super(toolHome);
    }

    @Extension
    public static class DescriptorImpl extends DescriptorBase<MavenAutoDiscoveryInstaller> {
        public DescriptorImpl() {
            super(Maven.MavenInstallation.class, new MavenDiscoveryOSX(), new MavenDiscoveryWindows());
        }
    }
}
