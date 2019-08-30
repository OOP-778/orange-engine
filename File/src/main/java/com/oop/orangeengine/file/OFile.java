package com.oop.orangeengine.file;

import com.oop.orangeengine.main.Engine;
import lombok.Getter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Getter
public class OFile {

    private String fileName;
    private File folder;
    private File file;

    public OFile(File folder, String fileName) {
        this.fileName = fileName;
        this.folder = folder;
    }

    public OFile(String fileName) {
        this.fileName = fileName;
    }

    public OFile(File file) {
        this.file = file;
        this.fileName = file.getName();

        if (file.getParentFile() != null)
            this.folder = file.getParentFile();
    }

    public OFile createIfNotExists() {
        return createIfNotExists(false);
    }

    public void rename(String to) {
        this.fileName = to;
        File dest = new File(file.getPath().replace(file.getName(), to));

        try {
            if (file != null) {
                Files.move(Paths.get(file.getPath()), Paths.get(dest.getPath()));
                this.file = new File(dest.getPath());
            } else file = folder != null ? new File(folder, fileName) : new File(fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public File getFile() {
        return file != null ? file : folder != null ? new File(folder, fileName) : new File(fileName);
    }

    public OFile createIfNotExists(boolean importFromResources) {

        try {

            if(folder != null && !folder.exists())
                folder.mkdirs();

            File file = folder == null ? new File(fileName) : new File(folder, fileName);
            if (!file.exists()) {

                if (importFromResources)
                    Engine.getInstance().getOwning().saveResource(fileName, true);

                else
                    file.createNewFile();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return this;

    }

    public void delete() {
        file.delete();
    }

}
