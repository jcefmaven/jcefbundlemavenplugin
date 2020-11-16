package org.cef.jcefbundlemavenplugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This goal creates bundles for Windows 64-Bit Platforms
 *
 * @author Fritz Windisch
 */
@Mojo(name = "win64", defaultPhase = LifecyclePhase.PACKAGE)
public class BundleNativesWin64Mojo extends BundleNativesWinLinuxMojo {
    public BundleNativesWin64Mojo() {
        super(NativeType.WIN64);
    }
}
