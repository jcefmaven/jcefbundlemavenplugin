package org.cef.jcefbundlemavenplugin.exporter;

import org.apache.maven.plugin.MojoFailureException;

/**
 * This class provides instances of all available bundle exporters.
 *
 * @author Fritz Windisch
 */
public enum ExportType {
    /**
     * The default exporter, leaving the bundle as it is. This exporter supports marking files as executable.
     */
    PLAIN(new PlainExporter()),
    /**
     * An exporter that exports the bundle as .zip file. This exporter does not mark files as executable.
     */
    ZIP(new ZipExporter()),
    /**
     * An exporter that exports the bundle as .tar.gz file. This exporter supports marking files in the .tar as executable.
     */
    TARGZ(new TarGzExporter());

    private Exporter exporter;

    ExportType(Exporter exporter) {
        this.exporter = exporter;
    }

    /**
     * Retrieves an exporter by its identifier/name
     *
     * @param name The name of the exporter
     * @return Exporter instance
     * @throws MojoFailureException If no exporter with this identifier/name exists
     */
    public static Exporter getExporterByName(String name) throws MojoFailureException {
        for (ExportType type : values()) {
            if (name.equalsIgnoreCase(type.name())) return type.getExporter();
        }
        throw new MojoFailureException("Invalid export type set: " + name);
    }

    /**
     * Get an exporter instance represented by this enum value.
     *
     * @return Exporter instance
     */
    public Exporter getExporter() {
        return exporter;
    }
}
