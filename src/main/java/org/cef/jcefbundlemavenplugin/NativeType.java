package org.cef.jcefbundlemavenplugin;

/**
 * Enumeration containing all supported natives.
 *
 * @author Fritz Windisch
 */
public enum NativeType {
    WIN32("win32"),
    WIN64("win64"),
    MACOSX64("macosx64"),
    LINUX32("linux32"),
    LINUX64("linux64");

    private String nativeName;

    NativeType(String nativeName) {
        this.nativeName = nativeName;
    }

    /**
     * Retrieves the platform dependent suffix for this native type.
     * This is used for building file paths.
     * @return nativeName
     */
    public String getNativeName() {
        return nativeName;
    }
}
