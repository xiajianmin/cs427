package org.jenkinsci.plugins.tools_autodiscover.modules;


import hudson.tasks.Maven.MavenInstallation;
import hudson.tools.ToolInstallation;
import org.jenkinsci.plugins.tools_autodiscover.ToolAutoDiscoveryModule;
import org.jenkinsci.plugins.tools_autodiscover.util.CommandRunner;
import org.jenkinsci.plugins.tools_autodiscover.util.Environment;
import org.jenkinsci.plugins.tools_autodiscover.util.PathUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maven discovery module for Windows compatible systems
 */
public class MavenDiscoveryWindows extends ToolAutoDiscoveryModule {

    /**
     * public no parameter constructor for MavenDiscoveryWindows
     */
    public MavenDiscoveryWindows() {
        super(new Environment());
    }

    /**
     * public constructor for MavenDiscoveryWindows
     *
     * @param environment: environment to be used, mostly important for performing mocks for cross OS testing
     */
    MavenDiscoveryWindows(Environment environment) {
        super(environment);
    }

    @Override
    public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
        return ((toolType == MavenInstallation.class) && env.isWindows());
    }


    @Override
    public List<ToolInstallation> getToolInstallations(Class<? extends ToolInstallation> toolType) {
        if (!isApplicable(toolType)) {
            return Collections.emptyList();
        }
        return Collections.<ToolInstallation>unmodifiableList(findPath());
    }

    /**
     * Find paths of all installed MVN's
     */
    public List<MavenInstallation> findPath() {
        List<MavenInstallation> path_list = new ArrayList<MavenInstallation>();
        String path = env.getPath();
        String[] paths = path.split("[;]+");

        for (String words : paths) {
            if (words.contains("maven")) {
                String mvnVersion = getMVNVersion(words);
                if (mvnVersion != null) {
                    MavenInstallation mvn =
                            new MavenInstallation(mvnVersion, PathUtils.Windows.getParentDir(words), null);
                    path_list.add(mvn);
                }
            }
        }
        return path_list;
    }

    /**
     * Returns String describing version of the installed maven located in the given Path
     *
     * @param pathToMaven path to bin holding maven file
     */
    String getMVNVersion(String pathToMaven) {
        String command = "\"" + pathToMaven + "\\mvn\" -version";
        List<String> toParse = new CommandRunner(env).run(command, null);
        if (toParse.size() <= 0) return null;

        String[] toReturn = toParse.get(0).split("[ ]+");
        return (toReturn[0] + toReturn[1] + toReturn[2]);
    }
}
