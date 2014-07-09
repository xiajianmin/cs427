package org.jenkinsci.plugins.tools_autodiscover.modules;


import hudson.tasks.Ant.AntInstallation;
import hudson.tools.ToolInstallation;
import org.jenkinsci.plugins.tools_autodiscover.ToolAutoDiscoveryModule;
import org.jenkinsci.plugins.tools_autodiscover.util.CommandRunner;
import org.jenkinsci.plugins.tools_autodiscover.util.Environment;
import org.jenkinsci.plugins.tools_autodiscover.util.StringConstants;
import org.jenkinsci.plugins.tools_autodiscover.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Apache Ant discovery module for Mac OS X compatible systems
 */
public class AntDiscoveryOSX extends ToolAutoDiscoveryModule {
    private File installationDir;

    /**
     * Constructs AntDiscoveryOSX module using system Environment bridge object.
     */
    public AntDiscoveryOSX() {
        this(new Environment());
    }

    /**
     * Constructor for dependency injection.
     *
     * @param environment Environment to be used
     */
    AntDiscoveryOSX(Environment environment) {
        this(environment, new File(StringConstants.MacOSX.ANT_DIR_PATH));
    }

    /**
     * Constructor for dependency injection.
     *
     * @param environment     Environment to be used
     * @param installationDir File object used to list installation directory. Must support listFiles() method.
     */
    AntDiscoveryOSX(Environment environment, File installationDir) {
        super(environment);
        this.installationDir = installationDir;
    }
    
    /**
     * Returns whether or not the given ToolInstallation is an Ant installation.
     * 
     * @param   toolType    The ToolInstallation in question. 
     * @return  Boolean indicating whether or not the tool is Ant.
     */
    @Override
    public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
        return ((toolType == AntInstallation.class) && env.isMacOSX());
    }

    /**
     *  Returns a List of detected Ant installations. Each item includes the version number and path on system.
     *  Takes in a ToolInstallation to check that the correct field in the Jenkins configue page will be populated.
     *  If it is the correct ToolInstallation, getToolInstallations will make a call to a helper method to do the work.  
     *
     *  @param  toolType    The ToolInstallation in question.
     *  @return List of detected AntInstallation objects that contain pertinent information.
     */
    @Override
    public List<ToolInstallation> getToolInstallations(Class<? extends ToolInstallation> toolType) {
        if (!isApplicable(toolType)) {
            return Collections.emptyList();
        }
        List<AntInstallation> instances = getLocalAntInstances();
        return Collections.<ToolInstallation>unmodifiableList(instances);
    }

    /**
     * Returns the list of locally available Ant instances. Must be running under Mac OS X.
     * Starts by going to a known directory where Ant instances can be found (/opt/local/share/java/) and collects
     * all paths to Ant folders using a regex. This list is passed to a helper method that will run "apache -version" on each
     * found path.
     *
     * @return List of AntInstallation objects that contain information describing each found instance.
     */
    private List<AntInstallation> getLocalAntInstances() {
        ArrayList<String> foundAntPaths = new ArrayList<String>();

        Pattern antPattern = Pattern.compile("apache-ant\\d*");
        File[] dirList = installationDir.listFiles();
        if (dirList == null) {
            return Collections.emptyList();
        }
        for (File dir : dirList) {
            Matcher m = antPattern.matcher(dir.getName());
            if (m.matches()) {
                foundAntPaths.add(StringConstants.MacOSX.ANT_DIR_PATH + dir.getName() + "/bin/ant");
            }
        }

        // No Ant instances found.
        if (foundAntPaths.size() == 0) {
            return Collections.emptyList();
        }

        return parseLocalAntInfos(foundAntPaths);

    }

    /**
     * Extracts the version information from the output of ant -version.
     *
     * @return string containing version information
     */
    String extractAntVersionInfo(String output) {
        // Characters 0 to 10 and 23 to 28 happens to be about where the version is located in the output of ant -version
        return output.substring(0, 10) + " " + output.substring(23, 28);
    }

    /**
     * Returns a list of AntInstallation objects compiled from running ant -version on each found Ant path.
     * Given a list of paths to Ant executables, this function runs ant -version to find out version information on each
     * instance. Output from -version is buffered into a string and parsed.
     *
     * @return List of AntInstallation objects.
     */
    private ArrayList<AntInstallation> parseLocalAntInfos(List<String> pathList) {
        ArrayList<AntInstallation> antObjects = new ArrayList<AntInstallation>();
        CommandRunner cmd = new CommandRunner(env);
        for (String path : pathList) {
            List<String> listOutput = cmd.run(path + " -version", null);
            String output = StringUtils.listToString(listOutput);
            if (output != null) {
                antObjects.add(new AntInstallation(extractAntVersionInfo(output), path, null));
            }
        }
        return antObjects;
    }
}
