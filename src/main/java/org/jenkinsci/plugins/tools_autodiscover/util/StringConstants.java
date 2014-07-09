package org.jenkinsci.plugins.tools_autodiscover.util;

/**
 * Configurable string constants used for implementation.
 */
public class StringConstants {

    /**
     * For Windows systems
     */
    public static class Windows {
        /**
         * Registry key path where list of keys for each JDK are stored
         */
        public final static String WIN_JDK_REG_KEY = "\"HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\"";

        /**
         * Registry path prefix for valid JDK entries.
         */
        public final static String WIN_JDK_REG_KEY_PREFIX = "HKEY_LOCAL_MACHINE\\SOFTWARE";
    }

    /**
     * For Mac OS X systems
     */
    public static class MacOSX {
        /**
         * Absolute path where the Maven instances should be located under MacPorts.
         */
        public final static String MAVEN_DIR_PATH = "/opt/local/share/java/";

        /**
         * Absolute path where the Ant instances should be located under MacPorts.
         */
        public final static String ANT_DIR_PATH = "/opt/local/share/java/";
    }
}
