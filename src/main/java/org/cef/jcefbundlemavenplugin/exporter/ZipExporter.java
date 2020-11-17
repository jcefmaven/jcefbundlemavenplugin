package org.cef.jcefbundlemavenplugin.exporter;

import org.apache.maven.plugin.MojoExecutionException;
import org.cef.jcefbundlemavenplugin.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * An exporter that exports the bundle as .zip file. This exporter does not mark files as executable.
 *
 * @author Fritz Windisch
 */
public class ZipExporter implements Exporter {
    @Override
    public void export(File directory, Function<String, Boolean> isExecutable) throws MojoExecutionException, IOException {
        File zipOutput = new File(directory.getParentFile(), directory.getName() + ".zip");
        if (!zipOutput.createNewFile()) throw new MojoExecutionException("Could not create file " + zipOutput);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipOutput));
        export(zos, directory, directory);
        zos.close();
        //Delete source directory
        FileUtil.deleteDir(directory);
    }

    private void export(ZipOutputStream zos, File base, File directory) throws IOException {
        String relative = base.toPath().relativize(directory.toPath()).toString();
        byte[] buff = new byte[4096];
        int r;
        if (directory.isDirectory()) {
            if (!directory.equals(base)) {
                zos.putNextEntry(new ZipEntry(relative + "/"));
                zos.closeEntry();
            }
            File[] files = directory.listFiles();
            if (files == null) return;
            for (File file : files) {
                export(zos, base, file);
            }
        } else {
            zos.putNextEntry(new ZipEntry(relative));
            FileInputStream fis = new FileInputStream(directory);
            while ((r = fis.read(buff)) != -1) {
                zos.write(buff, 0, r);
            }
            fis.close();
            zos.closeEntry();
        }
    }
}
