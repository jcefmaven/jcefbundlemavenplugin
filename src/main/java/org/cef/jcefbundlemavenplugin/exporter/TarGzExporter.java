package org.cef.jcefbundlemavenplugin.exporter;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.maven.plugin.MojoExecutionException;
import org.cef.jcefbundlemavenplugin.util.FileUtil;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TarGzExporter implements Exporter {
    @Override
    public void export(File directory, Function<String, Boolean> isExecutable) throws MojoExecutionException, IOException {
        File tarOutput = new File(directory.getParentFile(), directory.getName()+".tar.gz");
        if(!tarOutput.createNewFile())throw new MojoExecutionException("Could not create file "+tarOutput);
        try (OutputStream fo = Files.newOutputStream(tarOutput.toPath());
             OutputStream gzo = new GzipCompressorOutputStream(fo);
             TarArchiveOutputStream zos = new TarArchiveOutputStream(gzo)) {
            zos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            export(zos, directory, directory, isExecutable);
        }
        //Delete source directory
        FileUtil.deleteDir(directory);
    }

    private void export(TarArchiveOutputStream zos, File base, File directory, Function<String, Boolean> isExecutable) throws IOException {
        String relative = base.toPath().relativize(directory.toPath()).toString();
        byte[] buff = new byte[4096];
        int r;
        if(directory.isDirectory()){
            if(!directory.equals(base)) {
                zos.putArchiveEntry(new TarArchiveEntry(relative + "/"));
                zos.closeArchiveEntry();
            }
            File[] files = directory.listFiles();
            if(files==null)return;
            for(File file : files){
                export(zos, base, file, isExecutable);
            }
        }else{
            TarArchiveEntry entry = new TarArchiveEntry(relative);
            if(isExecutable.apply(relative)){
                entry.setMode(00100755); //Set executable bit for all users
            }
            entry.setSize(directory.length());
            zos.putArchiveEntry(entry);
            FileInputStream fis = new FileInputStream(directory);
            while((r=fis.read(buff))!=-1){
                zos.write(buff, 0, r);
            }
            fis.close();
            zos.closeArchiveEntry();
        }
    }
}
