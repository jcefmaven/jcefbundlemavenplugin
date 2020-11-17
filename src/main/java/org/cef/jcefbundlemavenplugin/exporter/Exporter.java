package org.cef.jcefbundlemavenplugin.exporter;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

/**
 * An exporter exports a generated bundle in a specified format.
 *
 * @author Fritz Windisch
 */
public interface Exporter {
    /**
     * Exports a bundle directory in a specified format
     *
     * @param directory    The bundle directory
     * @param isExecutable A function indicating which files need to be executable
     *                     (relative path -> should be executable?)
     * @throws MojoExecutionException If a file operation fails
     * @throws IOException            If a operation fails
     */
    void export(File directory, Function<String, Boolean> isExecutable) throws MojoExecutionException, IOException;
}
