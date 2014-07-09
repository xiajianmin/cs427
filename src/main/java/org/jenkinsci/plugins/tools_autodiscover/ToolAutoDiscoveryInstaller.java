package org.jenkinsci.plugins.tools_autodiscover;

import hudson.FilePath;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolInstallerDescriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Base class for ToolInstaller objects using ToolAutoDiscoveryModule.
 * <p/>
 * Rather than actually installing the tool, performInstallation() simply returns
 * the known path of the existing tool. {@link DescriptorBase} is responsible for actual
 * discovery process.
 */
@SuppressWarnings("unchecked")
public abstract class ToolAutoDiscoveryInstaller extends ToolInstaller {
    private final String toolHome;

    /**
     * Construct a ToolAutoDiscoveryInstaller based on given home path.
     *
     * @param toolHome Home path of the tool to be "installed."
     */
    @DataBoundConstructor
    public ToolAutoDiscoveryInstaller(String toolHome) {
        super(""); // only allow master node for installation
        this.toolHome = toolHome;
    }

    /**
     * Returns home path of the tool managed by this installer.
     */
    public String getToolHome() {
        return toolHome;
    }

    @Override
    public FilePath performInstallation(ToolInstallation toolInstallation, Node node, TaskListener taskListener) throws IOException, InterruptedException {
        return new FilePath(new File(toolHome));
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorBase getDescriptor() {
        return (DescriptorBase) super.getDescriptor();
    }

    /**
     * Base implementation class for descriptor objects of ToolAutoDiscoveryInstaller subclasses.
     * <p/>
     * In each subclass of ToolAutoDiscoveryInstaller, create a subclass of DescriptorBase
     * then fix it with Jenkins @Extension annotation for automatic discovery.
     */
    public static class DescriptorBase<T extends ToolAutoDiscoveryInstaller> extends ToolInstallerDescriptor<T> {
        private List<ToolAutoDiscoveryModule> discoveryModules;
        protected transient Class<? extends ToolInstallation> toolType;

        /**
         * Construct Descriptor for given tool type and discovery modules.
         *
         * @param toolType         tool type of the configuration panel where the descriptor registers to
         * @param discoveryModules discovery modules for toolType
         */
        public DescriptorBase(Class<? extends ToolInstallation> toolType,
                              ToolAutoDiscoveryModule... discoveryModules) {
            this.toolType = toolType;
            this.discoveryModules = new ArrayList<ToolAutoDiscoveryModule>(Arrays.asList(discoveryModules));
        }

        protected List<ToolInstallation> getToolInstallations(Class<? extends ToolInstallation> toolType) {
            List<ToolInstallation> list = new ArrayList<ToolInstallation>();
            Set<String> knownPaths = new HashSet<String>();
            for (ToolAutoDiscoveryModule module : discoveryModules) {
                if (!module.isApplicable(toolType)) {
                    continue;
                }

                List<ToolInstallation> foundTools = module.getToolInstallations(toolType);
                // Use HashSet to filter already known paths.
                // Currently, path identity test is not performed. For example,
                // "/test" and "//test" are not considered the same.
                for (ToolInstallation tool : foundTools) {
                    if (knownPaths.contains(tool.getHome())) {
                        continue;
                    }
                    knownPaths.add(tool.getHome());
                    list.add(tool);
                }
            }
            return list;
        }

        /**
         * Returns all ToolInstallation instances found.
         *
         * @return List of ToolInstallation found.
         */
        public List<ToolInstallation> getToolInstallations() {
            return getToolInstallations(getToolType());
        }

        /**
         * Returns tool type of this descriptor
         *
         * @return Class object representing the tool type
         */
        public Class<? extends ToolInstallation> getToolType() {
            return toolType;
        }

        /**
         * Get display name of the tool type of this descriptor.
         * <p/>
         * This is used for human-readable interfaces.
         *
         * @return human-readable display name of the tool type (e.g. JDK, Maven)
         */
        public String getToolTypeDisplayName() {
            return Jenkins.getInstance().getDescriptor(toolType).getDisplayName();
        }

        @Override
        public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
            return !getToolInstallations(toolType).isEmpty();
        }

        @Override
        public String getDisplayName() {
            return "Use Local Installation";
        }
    }
}
