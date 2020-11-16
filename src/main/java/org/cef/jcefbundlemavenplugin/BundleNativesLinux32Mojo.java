package org.cef.jcefbundlemavenplugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This goal creates bundles for Linux 32-Bit Platforms
 *
 * @author Fritz Windisch
 */
@Mojo(name = "linux32", defaultPhase = LifecyclePhase.PACKAGE)
public class BundleNativesLinux32Mojo extends BundleNativesWinLinuxMojo {
    public BundleNativesLinux32Mojo() {
        super(NativeType.LINUX32);
    }
}
