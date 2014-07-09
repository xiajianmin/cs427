package org.jenkinsci.plugins.tools_autodiscover;

import hudson.Extension;
import hudson.tasks.Ant;
import org.jenkinsci.plugins.tools_autodiscover.modules.AntDiscoveryOSX;
import org.jenkinsci.plugins.tools_autodiscover.modules.AntDiscoveryWindows;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Automatic discovery installer for Apache Ant.
 */
@SuppressWarnings("unchecked")
public class AntAutoDiscoveryInstaller extends ToolAutoDiscoveryInstaller {
    @DataBoundConstructor
    public AntAutoDiscoveryInstaller(String toolHome) {
        super(toolHome);
    }

    @Extension
    public static class DescriptorImpl extends DescriptorBase<AntAutoDiscoveryInstaller> {
        public DescriptorImpl() {
            super(Ant.AntInstallation.class, new AntDiscoveryOSX(), new AntDiscoveryWindows());
        }
    }
}
