package org.jenkinsci.plugins.tools_autodiscover.modules;


import hudson.model.JDK;
import hudson.tools.ToolInstallation;
import org.jenkinsci.plugins.tools_autodiscover.ToolAutoDiscoveryModule;
import org.jenkinsci.plugins.tools_autodiscover.util.CommandRunner;
import org.jenkinsci.plugins.tools_autodiscover.util.Environment;
import org.jenkinsci.plugins.tools_autodiscover.util.StringConstants;

import java.io.File;
import java.util.*;

/**
 * JDK discovery module for Windows compatible systems
 */
public class JDKDiscoveryWindows extends ToolAutoDiscoveryModule {
    /**
     * Public no parameter constructor for JDKDiscoveryWindows
     */
    public JDKDiscoveryWindows() {
        this(new Environment());
    }

    /**
     * Public constructor for JDKDiscoveryWindows
     *
     * @param environment environment to use, mostly important for performing mocks for cross OS testing
     */
    JDKDiscoveryWindows(Environment environment) {
        super(environment);
    }

    @Override
    public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
        return (toolType == JDK.class) && env.isWindows();
    }

    @Override
    public List<ToolInstallation> getToolInstallations(Class<? extends ToolInstallation> toolType) {
        if (!isApplicable(toolType)) {
            return Collections.emptyList();
        }

        return Collections.<ToolInstallation>unmodifiableList(filterJDKList(findJava()));
    }

    /**
     * Filter JDK paths to remove duplicates and non-existing paths.
     *
     * @param jdkList List of JDK to be filtered
     * @return filtered List of JDK paths
     */
    List<JDK> filterJDKList(List<JDK> jdkList) {
        Map<String, JDK> noDups = new HashMap<String, JDK>();
        List<JDK> returner = new ArrayList<JDK>();

        for (JDK loc : jdkList) {
            noDups.put(loc.getHome(), loc);
        }

        for (String path : noDups.keySet()) {
            if (new File(path).exists()) {
                returner.add(noDups.get(path));
            }
        }

        return returner;
    }

    /**
     * Returns JDK's found on the system.
     * <p/>
     * Parses the return value of reqQueryParse() to find the registry key of each JDK, which is
     * then used to find its path.
     *
     * @return List of JDK found in registry
     */
    List<JDK> findJava() {

        List<String> regLoc = regQueryParse(StringConstants.Windows.WIN_JDK_REG_KEY);
        List<JDK> returner = new ArrayList<JDK>();
        regLoc.remove(1);

        for (String loc : regLoc) {
            if (loc.contains(StringConstants.Windows.WIN_JDK_REG_KEY_PREFIX)) {
                String version = getJDKVersion(loc);
                loc = (regQueryParse("\"" + loc + "\"").get(2));
                loc = loc.substring(loc.indexOf(":\\") - 1);
                JDK what = new JDK(version, loc);
                returner.add(what);
            }
        }

        return returner;
    }

    /**
     * Runs REG QUERY command and returns the results to be parsed.
     *
     * @param key key to run REG QUERY on
     * @return output of REG QUERY in List<String> form
     */
    private List<String> regQueryParse(String key) {
        return CommandRunner.run("REG QUERY " + key);
    }

    /**
     * Helper function gets version of JDK found in given path
     *
     * @param pathToJDK path of JDK to acquire version from
     * @return version of JDK
     */
    String getJDKVersion(String pathToJDK) {
        return pathToJDK.substring(pathToJDK.lastIndexOf("\\") + 1);
    }
}
