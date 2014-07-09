package org.jenkinsci.plugins.tools_autodiscover;

import hudson.Extension;
import hudson.model.JDK;
import org.jenkinsci.plugins.tools_autodiscover.modules.JDKDiscoveryOSX;
import org.jenkinsci.plugins.tools_autodiscover.modules.JDKDiscoveryWindows;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Automatic discovery installer for JDK.
 */
@SuppressWarnings("unchecked")
public class JDKAutoDiscoveryInstaller extends ToolAutoDiscoveryInstaller {
    @DataBoundConstructor
    public JDKAutoDiscoveryInstaller(String toolHome) {
        super(toolHome);
    }

    @Extension
    public static class DescriptorImpl extends DescriptorBase<JDKAutoDiscoveryInstaller> {
        public DescriptorImpl() {
            super(JDK.class, new JDKDiscoveryOSX(), new JDKDiscoveryWindows());
        }
    }
}
