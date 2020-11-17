package org.cef.jcefbundlemavenplugin.exporter;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.function.Function;

/**
 * The default exporter, leaving the bundle as it is. This exporter supports marking files as executable.
 *
 * @author Fritz Windisch
 */
public class PlainExporter implements Exporter {
    @Override
    public void export(File directory, Function<String, Boolean> isExecutable) throws MojoExecutionException {
        export(directory, directory, isExecutable);
    }

    private void export(File base, File directory, Function<String, Boolean> isExecutable) throws MojoExecutionException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files == null) return;
            for (File file : files) {
                export(base, file, isExecutable);
            }
        } else {
            String relative = base.toPath().relativize(directory.toPath()).toString();
            if (isExecutable.apply(relative)) {
                if (!directory.setExecutable(true))
                    throw new MojoExecutionException("Failed to mark file as executable! " + directory);
            }
        }
    }
}
