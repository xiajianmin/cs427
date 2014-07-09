package org.jenkinsci.plugins.tools_autodiscover.util;

import java.util.List;

/**
 * String utility functions.
 */
public class StringUtils {
    /**
     * Concatenates every String in the input list.
     * @param   list    A list of a Strings to be concatenated together.
     * @return  The passed in list of Strings as a singular String.
     */
    public static String listToString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
        }
        return sb.toString();
    }
}
