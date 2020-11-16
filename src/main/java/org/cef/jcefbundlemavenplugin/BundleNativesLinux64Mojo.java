package org.cef.jcefbundlemavenplugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This goal creates bundles for Linux 64-Bit Platforms
 *
 * @author Fritz Windisch
 */
@Mojo(name = "linux64", defaultPhase = LifecyclePhase.PACKAGE)
public class BundleNativesLinux64Mojo extends BundleNativesWinLinuxMojo {
    public BundleNativesLinux64Mojo() {
        super(NativeType.LINUX64);
    }
}
