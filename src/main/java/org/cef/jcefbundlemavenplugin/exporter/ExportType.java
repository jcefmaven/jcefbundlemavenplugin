package org.cef.jcefbundlemavenplugin.exporter;

import org.apache.maven.plugin.MojoFailureException;

public enum ExportType {
    PLAIN(new PlainExporter()),
    ZIP(new ZipExporter()),
    TARGZ(new TarGzExporter());

    private Exporter exporter;

    ExportType(Exporter exporter) {
        this.exporter = exporter;
    }

    public Exporter getExporter() {
        return exporter;
    }

    public static Exporter getExporterByName(String name) throws MojoFailureException {
        for(ExportType type : values()){
            if(name.equalsIgnoreCase(type.name()))return type.getExporter();
        }
        throw new MojoFailureException("Invalid export type set: "+name);
    }
}
