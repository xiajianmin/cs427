package org.jenkinsci.plugins.tools_autodiscover.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Helper class to simplify command execution and output parsing.
 */
public class CommandRunner {
    private Environment env;

    /**
     * Build CommandRunner which uses specified environment.
     *
     * @param environment Environment to be used.
     */
    public CommandRunner(Environment environment) {
        env = environment;
    }

    /**
     * Run specified command with environment variables.
     *
     * @param toRun command to run
     * @param envp  array of "VAR=VALUE" entries for environment variables, or null to inherit environment variables
     * @return List containing every line from stdout in order
     */
    public List<String> run(String toRun, String[] envp) {
        ArrayList<String> results = new ArrayList<String>();
        try {
            Process proc = env.exec(toRun, envp, null);
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                results.add(line);
            }

            return results;
        } catch (IOException ex) {
            // Report Problems
            // ...
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Run specified command.
     * <p/>
     * This is a shortcut for instance method {@link #run(String, String[])}.
     *
     * @param toRun command to run
     * @return List containing every line from stdout in order
     */
    public static List<String> run(String toRun) {
        return new CommandRunner(new Environment()).run(toRun, null);
    }
}
