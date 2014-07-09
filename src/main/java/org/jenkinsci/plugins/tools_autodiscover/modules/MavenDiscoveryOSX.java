package org.jenkinsci.plugins.tools_autodiscover.modules;


import hudson.tasks.Maven.MavenInstallation;
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
 * Maven discovery module for Mac OS X compatible systems
 */
public class MavenDiscoveryOSX extends ToolAutoDiscoveryModule {
    private File installationDir;
    
    /**
     * Constructs MavenDiscoveryOSX module using system Environment bridge object.
     */
    public MavenDiscoveryOSX() {
        this(new Environment());
    }
    
    /**
     * Constructor for dependency injection.
     *
     * @param environment Environment to be used
     */
    MavenDiscoveryOSX(Environment environment) {
        this(environment, new File(StringConstants.MacOSX.MAVEN_DIR_PATH));
    }

    
    /**
     * Constructor for dependency injection.
     *
     * @param environment     Environment to be used
     * @param installationDir File object used to list installation directory. Must support listFiles() method.
     */
    MavenDiscoveryOSX(Environment environment, File installationDir) {
        super(environment);
        this.installationDir = installationDir;
    }

    /**
     * Returns whether or not the given ToolInstallation is an Maven installation.
     * 
     * @param   toolType    The ToolInstallation in question. 
     * @return  Boolean indicating whether or not the tool is Maven.
     */
    @Override
    public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
        return ((toolType == MavenInstallation.class) && env.isMacOSX());
    }
    /**
     *  Returns a List of detected Maven installations. Each item includes the version number and path on system.
     *  Takes in a ToolInstallation to check that the correct field in the Jenkins configue page will be populated.
     *  If it is the correct ToolInstallation, getToolInstallations will make a call to a helper method to do the work.  
     *
     *  @param  toolType    The ToolInstallation in question.
     *  @return List of detected MavenInstallation objects that contain pertinent information.
     */
    @Override
    public List<ToolInstallation> getToolInstallations(Class<? extends ToolInstallation> toolType) {
        if (!isApplicable(toolType)) {
            return Collections.emptyList();
        }
        List<MavenInstallation> instances = getLocalMavenInstances();
        return Collections.<ToolInstallation>unmodifiableList(instances);
    }

    /**
     * Returns the list of locally available Maven instances. Must be running under Mac OS X.
     * Starts by going to a known directory where maven instances can be found (/opt/local/share/java/) and collects
     * all paths to maven folders using a regex. This list is passed to a helper method that will run "mvn -v" on each
     * found path.
     *
     * @return List of MavenInstallation objects that contain information describing each found instance.
     */
    protected List<MavenInstallation> getLocalMavenInstances() {

        ArrayList<String> foundMavenPaths = new ArrayList<String>();

        Pattern mavenPattern = Pattern.compile("maven\\d*");
        File[] dirList = installationDir.listFiles();
        if (dirList == null) {
            return Collections.emptyList();
        }
        for (File file : dirList) {
            Matcher m = mavenPattern.matcher(file.getName());
            if (m.matches()) {
                foundMavenPaths.add(StringConstants.MacOSX.MAVEN_DIR_PATH + file.getName() + "/bin/mvn");
            }
        }

        // No maven instances found.
        if (foundMavenPaths.size() == 0) {
            return Collections.emptyList();
        }

        return parseLocalMavenInfos(foundMavenPaths);

    }

    /**
     * Extracts the version information from the output of mvn%d -v.
     *
     * @return string containing version information
     */
    String extractMavenVersionInfo(String output) {
        return output.substring(0, 18);
    }

    /**
     * Returns a list of MavenInstallation objects compiled from running mvn# -v on each found maven path.
     * Given a list of paths to Maven executables, this function runs mvn -v to find out version information on each
     * instance. Output from -v is buffered into a string and parsed.
     *
     * @return List of MavenInstallation objects.
     */
    private List<MavenInstallation> parseLocalMavenInfos(List<String> paths) {
        List<MavenInstallation> mavenObjects = new ArrayList<MavenInstallation>();
        CommandRunner cmd = new CommandRunner(env);
        for (String mavenPath : paths) {
            List<String> listOutput = cmd.run(mavenPath + " -v", null);
            String output = StringUtils.listToString(listOutput);
            if (output != null)
                mavenObjects.add(new MavenInstallation(extractMavenVersionInfo(output), mavenPath, null));
        }

        return mavenObjects;
    }
}
