package org.jenkinsci.plugins.tools_autodiscover.modules;

import hudson.tasks.Ant;
import hudson.tools.ToolInstallation;
import org.jenkinsci.plugins.tools_autodiscover.ToolAutoDiscoveryModule;
import org.jenkinsci.plugins.tools_autodiscover.util.CommandRunner;
import org.jenkinsci.plugins.tools_autodiscover.util.Environment;
import org.jenkinsci.plugins.tools_autodiscover.util.PathUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Apache Ant discovery module for Microsoft Windows systems.
 */
public class AntDiscoveryWindows extends ToolAutoDiscoveryModule {
    // pattern is digits.digits.digits
    private static final Pattern versionPattern = Pattern.compile("\\b\\d+\\.\\d+\\.\\d+\\b");

    /**
     * Default constructor. Uses default Environment bridge object.
     */
    public AntDiscoveryWindows() {
        this(new Environment());
    }

    /**
     * Constructor with supplied Environment.
     * <p/>
     * Primarily used for dependency injection testing.
     *
     * @param environment Environment to be used for the module
     */
    AntDiscoveryWindows(Environment environment) {
        super(environment);
    }

    List<String> parsePath(String pathVar) {
        if (pathVar == null) {
            throw new IllegalArgumentException("pathVar is null");
        }
        // we hard-code Windows file separator for consistent behavior
        StringTokenizer tokenizer = new StringTokenizer(pathVar, ";");
        List<String> pathList = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            pathList.add(tokenizer.nextToken());
        }
        return pathList;
    }

    String parseVersion(String s) {
        if (s == null) {
            return null;
        } else if (!s.toLowerCase().contains("ant(tm)")) {
            return null;
        }

        Matcher matcher = versionPattern.matcher(s);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private List<String> getPathEntries() {
        return parsePath(env.getPath());
    }

    @Override
    public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
        return (toolType == Ant.AntInstallation.class) && env.isWindows();
    }

    @Override
    public List<ToolInstallation> getToolInstallations(Class<? extends ToolInstallation> toolType) {
        if (!isApplicable(toolType)) {
            return Collections.emptyList();
        }

        List<ToolInstallation> antList = new ArrayList<ToolInstallation>();
        // check every entry in PATH
        for (String path : getPathEntries()) {
            if (!path.toLowerCase().endsWith("\\bin")) {
                // ant executable files reside in %ANT_HOME%/bin
                continue;
            }

            String executable = "\"" + path.replace("\"", "\\\"") + "\\ant\"";
            List<String> output = new CommandRunner(env).run(executable + " -version", null);
            if (output == null) {
                continue;
            }

            // Parse the output to see whether it contains valid ant version
            for (String line : output) {
                String version = parseVersion(line);
                if (version != null) {
                    antList.add(new Ant.AntInstallation("Ant " + version, PathUtils.Windows.getParentDir(path), null));
                    break;
                }
            }
        }

        return Collections.unmodifiableList(antList);
    }

}
