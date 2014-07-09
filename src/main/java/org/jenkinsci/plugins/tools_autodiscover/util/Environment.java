package org.jenkinsci.plugins.tools_autodiscover.util;

import java.io.File;
import java.io.IOException;

/**
 * Utility class providing information on environment.
 */
public class Environment {
    /**
     * Tests whether current environment is Microsoft Windows.
     *
     * @return true if running on Windows; false otherwise
     */
    public boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }

    /**
     * Tests whether current environment is Mac OS X.
     *
     * @return true if running on Mac OS X; false otherwise
     */
    public boolean isMacOSX() {
        return System.getProperty("os.name").contains("OS X");
    }

    /**
     * Returns the PATH environment variable
     *
     * @return String containing PATH environment variable
     */
    public String getPath() {
        return System.getenv("PATH");
    }

    /**
     * Executes the given command under specified environment and working directory.
     * <p/>
     * This is a wrapper over {@link java.lang.Runtime#exec(String, String[], java.io.File)}.
     * See {@link java.lang.Runtime#exec(String, String[], java.io.File)} for details.
     */
    public Process exec(String command, String[] env, File dir)
            throws IOException {
        if (isWindows()) {
            command = System.getenv("COMSPEC") + " /C " + command;
        }
        return Runtime.getRuntime().exec(command, env, dir);
    }
}
