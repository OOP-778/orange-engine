package com.oop.orangeengine.main.util;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarUtil {
    public static final char JAR_SEPARATOR = File.separatorChar;

    public static void copyFolderFromJar(String folderName, File destFolder, CopyOption option, Class source) {
        try {
            if (!destFolder.exists())
                destFolder.mkdirs();

            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(getFullPath(source)));

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.getName().startsWith(folderName + JAR_SEPARATOR))
                    continue;

                String fileName = entry.getName();

                if (fileName.charAt(fileName.length() - 1) == JAR_SEPARATOR) {
                    File file = new File(destFolder + File.separator + fileName);
                    if (file.isFile()) {
                        file.delete();
                    }
                    file.mkdirs();
                    continue;
                }

                File file = new File(destFolder + File.separator + fileName);
                if (option == CopyOption.COPY_IF_NOT_EXIST && file.exists())
                    continue;

                if (!file.getParentFile().exists())
                    file.getParentFile().mkdirs();

                if (!file.exists())
                    file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }

            zis.closeEntry();
            zis.close();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to copy folder with name " + folderName, ex);
        }
    }

    public enum CopyOption {
        COPY_IF_NOT_EXIST, REPLACE_IF_EXIST
    }

    @FunctionalInterface
    public interface PathTrimmer {
        String trim(String original);
    }

    public static void copyFileFromJar(String fileName, File destFolder, CopyOption copyOption, String outName, Class<?> source) {

        if (!destFolder.exists())
            destFolder.mkdirs();

        InputStream inputStream;
        FileOutputStream outputStream = null;

        File out = new File(destFolder, outName == null ? fileName : outName);
        try {
            inputStream = findFileAsInputStream(fileName, source);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to find file by name: " + fileName);
        }

        if (out.exists() && copyOption == CopyOption.COPY_IF_NOT_EXIST) return;

        try {
            outputStream = new FileOutputStream(out);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failed to open output stream " + fileName, e);
        }

        try {
            while (inputStream.available() > 0)
                outputStream.write(inputStream.read());

            inputStream.close();
            outputStream.close();

        } catch (Exception ex) {
            throw new IllegalStateException("Failed to copy file with name " + fileName, ex);
        }

    }

    public static File getFullPath(Class<?> source) {
        try {
            String path = source.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8").replace(" ", "%20");

            if (!decodedPath.startsWith("file"))
                decodedPath = "file://" + decodedPath;
            return new File(new URI(decodedPath));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static InputStream findFileAsInputStream(String name, Class klass) throws IOException {
        File actualFile = getFullPath(klass);
        final JarFile jar = new JarFile(actualFile);

        final Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {

            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith(name))
                return jar.getInputStream(entry);
        }

        jar.close();
        return null;
    }

    public static void copyFileFromJar(String fileName, File destFolder, CopyOption copyOption, Class<?> source) {
        copyFileFromJar(fileName, destFolder, copyOption, null, source);
    }

    public static void copyFileFromJar(String fileName, File destFolder, Class<?> source) {
        copyFileFromJar(fileName, destFolder, CopyOption.COPY_IF_NOT_EXIST, null, source);
    }
}
