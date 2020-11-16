package org.cef.jcefbundlemavenplugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This goal creates bundles for Windows 32-Bit Platforms
 *
 * @author Fritz Windisch
 */
@Mojo(name = "win32", defaultPhase = LifecyclePhase.PACKAGE)
public class BundleNativesWin32Mojo extends BundleNativesWinLinuxMojo {
    public BundleNativesWin32Mojo() {
        super(NativeType.WIN32);
    }
}
