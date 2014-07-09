package org.jenkinsci.plugins.tools_autodiscover.util;

/**
 * Utility class for path manipulation.
 */
public class PathUtils {
    /**
     * Contains helper methods specific to Windows platform.
     */
    public static class Windows {
        /**
         * Find parent component of given Windows path.
         */
        public static String getParentDir(String path) {
            String pathSeparator = "\\";
            int pos = path.lastIndexOf(pathSeparator);
            if (pos == -1) {
                return "";
            }
            return path.substring(0, pos);
        }
    }
}
