package org.cef.jcefbundlemavenplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.cef.jcefbundlemavenplugin.util.FileUtil;
import org.cef.jcefbundlemavenplugin.util.Relocator;

import java.io.File;
import java.io.IOException;

/**
 * This is the parent class for all bundles on Linux and Windows platforms.
 *
 * @author Fritz Windisch
 */
public class BundleNativesWinLinuxMojo extends BundleNativesMojo {
    /**
     * The folder relative to the bundle folder, where the natives should reside.
     * This has to be the same as the argument in the call to JCefLoader.installAndLoadCef(new File("cef_bundle"));
     */
    @Parameter(defaultValue = "cef_bundle")
    private String relativeBundlePath;

    /**
     * Relocations to perform (e.g. copy jar file to bundle, or copy resources).
     * Entries are described as relative paths from:to, delimited by ":".<br/>
     * From path: Relative to your projects build directory (e.g. target/)<br/>
     * To path: Relative to your bundle folder (e.g. target/jcef-bundle-X/)
     */
    @Parameter(required = true)
    private String[] relocations;

    public BundleNativesWinLinuxMojo(NativeType nativeType) {
        super(nativeType);
    }

    @Override
    public void bundle(File natives, File targetDir) throws IOException, MojoFailureException, MojoExecutionException {
        //Delete previous versions
        if (targetDir.exists()) FileUtil.deleteDir(targetDir);
        File bundleDir = new File(targetDir, relativeBundlePath);
        //Extract bundle
        FileUtil.unzip(natives, bundleDir);
        File extracted = new File(bundleDir, "jcef-natives-" + getNativeType().getNativeName() + ".jar");
        FileUtil.unzip(extracted, bundleDir);
        if (!extracted.delete()) throw new IOException("Failed to delete " + extracted);
        //Mark the bundle as installed
        if (!new File(bundleDir, "install.lock").createNewFile())
            throw new MojoExecutionException("Failed to create bundle lock file!");
        //Relocate jar and resources
        Relocator.relocate(new File(getProjectBuildDir()), targetDir, relocations);
        //Run exporter
        if (getNativeType() == NativeType.LINUX32 || getNativeType() == NativeType.LINUX64) {
            getExporter().export(targetDir, p -> p.endsWith("jcef_helper"));
        } else {
            getExporter().export(targetDir, p -> false);
        }
    }
}
