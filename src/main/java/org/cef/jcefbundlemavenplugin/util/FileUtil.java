package org.cef.jcefbundlemavenplugin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Util class providing frequently needed file operations
 *
 * @author Fritz Windisch
 */
public class FileUtil {
    /**
     * Unzips a file into another directory
     *
     * @param file The file to unzip
     * @param dir  The target directory
     * @throws IOException When a file operation can not be completed
     */
    public static void unzip(File file, File dir) throws IOException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
        byte[] buff = new byte[4096];
        int r;
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory() || entry.getName().isEmpty()) {
                zis.closeEntry();
                continue;
            }
            File f = new File(dir, entry.getName());
            if (!f.getParentFile().exists()) {
                if (!f.getParentFile().mkdirs()) throw new IOException("Failed to create dir " + f.getParentFile());
            }
            if (!f.createNewFile()) throw new IOException("Failed to create file " + f);
            FileOutputStream fos = new FileOutputStream(f);
            while ((r = zis.read(buff)) != -1) {
                fos.write(buff, 0, r);
            }
            fos.flush();
            fos.close();
            zis.closeEntry();
        }
        zis.close();
    }

    /**
     * Utility method to delete a directory recursively
     *
     * @param dir The directory to delete
     */
    public static void deleteDir(File dir) throws IOException {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File file : files) {
                deleteDir(file);
            }
        }
        if (!dir.delete()) throw new IOException("Failed to delete " + dir);
    }

    /**
     * Copies a directory or file to another directory or file
     *
     * @param from The file or directory to copy
     * @param to   The destination
     * @throws IOException When a file operation fails
     */
    public static void copyDir(File from, File to) throws IOException {
        if (from.isDirectory()) {
            if (!to.exists() && !to.mkdirs()) throw new IOException("Failed to create dir " + to);
            File[] files = from.listFiles();
            if (files == null) return;
            for (File f : files) {
                copyDir(f, new File(to, f.getName()));
            }
        } else {
            if (!to.getParentFile().exists()) {
                if (!to.getParentFile().mkdirs()) {
                    throw new IOException("Failed to create dir " + to.getParentFile());
                }
            } else if (to.isDirectory()) {
                to = new File(to, from.getName());
            }
            if (!to.createNewFile()) throw new IOException("Failed to create file " + to);
            FileInputStream fis = new FileInputStream(from);
            FileOutputStream fos = new FileOutputStream(to);
            byte[] buff = new byte[4096];
            int r;
            while ((r = fis.read(buff)) != -1) {
                fos.write(buff, 0, r);
            }
            fos.flush();
            fis.close();
            fos.close();
        }
    }
}
