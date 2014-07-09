package org.jenkinsci.plugins.tools_autodiscover;

import hudson.tools.ToolInstallation;
import org.jenkinsci.plugins.tools_autodiscover.util.Environment;

import java.util.List;

/**
 * Base class for classes that discovers local tool installations.
 */
public abstract class ToolAutoDiscoveryModule {
    /**
     * Environment bridge used by the module. Use this object instead of
     * {@link java.lang.Runtime}.
     */
    protected final Environment env;

    /**
     * Constructor. Takes Environment bridge as an argument for dependency injection.
     *
     * @param environment Environment bridge object for the module.
     */
    public ToolAutoDiscoveryModule(Environment environment) {
        this.env = environment;
    }

    /**
     * Tests whether this module is applicable for given tool type and the environment
     * which Jenkins is running on.
     *
     * @param toolType class type of ToolInstallation for test
     * @return true if the module can be used; false otherwise
     */
    public abstract boolean isApplicable(Class<? extends ToolInstallation> toolType);

    /**
     * Returns the list of all tool installations of given type found by the module.
     *
     * @param toolType class type of ToolInstallation to be found
     * @return List of all ToolInstallation instances of given type found by the module
     */
    public abstract List<ToolInstallation> getToolInstallations(Class<? extends ToolInstallation> toolType);
}
