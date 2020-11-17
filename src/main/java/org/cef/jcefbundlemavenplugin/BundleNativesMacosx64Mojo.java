package org.cef.jcefbundlemavenplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.cef.jcefbundlemavenplugin.util.FileUtil;
import org.cef.jcefbundlemavenplugin.util.InputStreamUtils;
import org.cef.jcefbundlemavenplugin.util.Relocator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This goal creates bundles for MacOS 64-Bit Platforms
 *
 * @author Fritz Windisch
 */
@Mojo(name = "macosx64", defaultPhase = LifecyclePhase.PACKAGE)
public class BundleNativesMacosx64Mojo extends BundleNativesMojo {
    /**
     * The folder relative to the bundle folder, where the natives should reside.
     * This has to be the same as the argument in the call to JCefLoader.installAndLoadCef(new File("cef_bundle"));
     */
    @Parameter(defaultValue = "jcef_app.app")
    private String bundleName;

    /**
     * The identifier of the MacOS Bundle
     */
    @Parameter(defaultValue = "org.cef.jcef")
    private String bundleIdentifier;

    /**
     * The name that MacOS will show the user for our Bundle
     */
    @Parameter(defaultValue = "jcef_app")
    private String bundleDisplayName;

    /**
     * A short version number (e.g. 1.0)
     */
    @Parameter(defaultValue = "${project.version}")
    private String bundleShortVersion;

    /**
     * A long version number, can be the same as short version number (e.g. 1.0)
     */
    @Parameter(defaultValue = "${project.version}")
    private String bundleVersion;

    /**
     * Copyright information for this bundle (e.g. &copy; Fritz Windisch)
     */
    @Parameter(defaultValue = "")
    private String bundleCopyright;

    /**
     * The main class of the bundle (e.g. org.cef.tests.MainFrame)
     */
    @Parameter(required = true)
    private String mainClass;

    /**
     * An array of options to pass to the JVM
     */
    @Parameter
    private String[] jvmOptions;

    /**
     * An array of arguments to pass to the embedded program
     */
    @Parameter
    private String[] jvmArgs;

    /**
     * Relocations to perform (e.g. copy jar file to bundle, or copy resources).
     * Entries are described as relative paths from:to, delimited by ":".<br/>
     * From path: Relative to your projects build directory (e.g. target/)<br/>
     * To path: Relative to your bundle folder, targeting the path for jar/class files
     * (e.g. target/jcef-bundle-macosx64/jcef_app.app/Contents/Java/)
     */
    @Parameter(required = true)
    private String[] relocations;

    public BundleNativesMacosx64Mojo() {
        super(NativeType.MACOSX64);
    }

    @Override
    public void bundle(File natives, File targetDir) throws IOException, MojoFailureException, MojoExecutionException {
        //Delete previous versions
        if (targetDir.exists()) FileUtil.deleteDir(targetDir);
        //Extract bundle
        FileUtil.unzip(natives, targetDir);
        File extracted = new File(targetDir, "jcef-natives-" + getNativeType().getNativeName() + ".jar");
        FileUtil.unzip(extracted, targetDir);
        if (!extracted.delete()) throw new IOException("Failed to delete " + extracted);
        File bundle = new File(targetDir, "jcef_app.app");
        File newBundle = new File(targetDir, bundleName);
        if (!bundleName.equals(bundle.getName())) {
            if (!bundle.renameTo(newBundle)) throw new MojoExecutionException("Could not rename bundle!");
        }
        //Adapt Contents/Info.plist
        File info = new File(newBundle, "Contents/Info.plist");
        String template = InputStreamUtils.readInputStreamToString(
                BundleNativesMacosx64Mojo.class.getResourceAsStream("/template.plist"),
                16 * 1024,
                StandardCharsets.UTF_8);
        String shortBundleName = bundleName.endsWith(".app") ?
                bundleName.substring(0, bundleName.length() - 4) : bundleName;
        String jvmOptions = compileArgs(this.jvmOptions);
        if (jvmOptions.contains("-Djava.library.path=")) {
            throw new MojoFailureException("You are not allowed to set a custom java.library.path for this bundle! " +
                    "Pack your libraries to Contents/Java/ instead (<relocation>X.dylib:X.dylib</relocation>)!");
        }
        String jvmArgs = compileArgs(this.jvmArgs);
        template = template.replace("{bundle_identifier}", bundleIdentifier);
        template = template.replace("{bundle_display_name}", bundleDisplayName);
        template = template.replace("{bundle_name}", shortBundleName);
        template = template.replace("{bundle_short_version}", bundleShortVersion);
        template = template.replace("{bundle_version}", bundleVersion);
        template = template.replace("{bundle_copyright}", bundleCopyright == null ? "" : bundleCopyright);
        template = template.replace("{main_class}", mainClass);
        template = template.replace("{jvm_options}", jvmOptions);
        template = template.replace("{jvm_args}", jvmArgs);
        FileOutputStream fos = new FileOutputStream(info);
        fos.write(template.getBytes(StandardCharsets.UTF_8));
        fos.flush();
        fos.close();
        //Copy license
        if (!new File(targetDir, "LICENSE.txt").renameTo(new File(newBundle, "Contents/LICENSE.txt")))
            throw new MojoExecutionException("Could not rename LICENSE.txt!");
        //Relocate jar and resources
        Relocator.relocate(new File(getProjectBuildDir()), new File(newBundle, "Contents/Java"), relocations);
        //Run exporter
        getExporter().export(targetDir, p -> p.contains("MacOS") || p.endsWith("Chromium Embedded Framework"));
    }

    /**
     * Converts an array of arguments to an xml string list for embedding in a .plist file
     *
     * @param args The arguments to convert
     * @return The xml list
     */
    private String compileArgs(String[] args) {
        if (args == null) return "";
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append("            <string>" + arg + "</string>\n");
        }
        return builder.toString();
    }
}
