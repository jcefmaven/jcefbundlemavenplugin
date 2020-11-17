package org.cef.jcefbundlemavenplugin.exporter;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public interface Exporter {
    void export(File directory, Function<String, Boolean> isExecutable) throws MojoExecutionException, IOException;
}
