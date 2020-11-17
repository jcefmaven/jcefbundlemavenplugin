package org.cef.jcefbundlemavenplugin.util;

import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;

/**
 * Util class that copies resources and classpath elements into bundles
 *
 * @author Fritz Windisch
 */
public class Relocator {
    /**
     * Performs a relocation for bundle resources and classpath elements, to set them up in the generated bundle.
     * The array of relocations are all relocation instructions from the plugin configuration.
     * <p>
     * A relocation is in the format A:B where A is a path to be copied from, and B is a path to be copied to.
     * Paths can be relative and absolute.
     *
     * @param from        The base dir of the left hand side of all relocation instructions
     * @param to          The base dir of the right hand side of all relocation instructions
     * @param relocations Array of relocation instructions
     * @throws MojoFailureException When the configuration is invalid
     * @throws IOException          When a file operation failed
     */
    public static void relocate(File from, File to, String[] relocations) throws MojoFailureException, IOException {
        for (String f : relocations) {
            relocate(from, to, f);
        }
    }

    /**
     * Performs a relocation for bundle resources and classpath elements, to set them up in the generated bundle.
     * The relocation is taken from the plugin configuration.
     * <p>
     * A relocation is in the format A:B where A is a path to be copied from, and B is a path to be copied to.
     * Paths can be relative and absolute.
     *
     * @param from       The base dir of the left hand side of all relocation instructions
     * @param to         The base dir of the right hand side of all relocation instructions
     * @param relocation The relocation instruction
     * @throws MojoFailureException When the configuration is invalid
     * @throws IOException          When a file operation failed
     */
    public static void relocate(File from, File to, String relocation) throws MojoFailureException, IOException {
        String[] split = relocation.split(":");
        if (split.length != 2)
            throw new MojoFailureException("Failed to parse relocation entry \"" + relocation + "\"");
        if (!split[0].isEmpty()) from = new File(from, split[0]);
        if (!split[1].isEmpty()) to = new File(to, split[1]);
        FileUtil.copyDir(from, to);
    }
}
