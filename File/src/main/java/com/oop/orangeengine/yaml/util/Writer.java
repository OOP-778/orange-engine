package com.oop.orangeengine.yaml.util;

import lombok.NonNull;
import lombok.SneakyThrows;
import sun.security.action.GetPropertyAction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;

public class Writer extends BufferedWriter {
    private final String lineSeparator = AccessController.doPrivileged(new GetPropertyAction("line.separator"));
    private String lastWritten = null;

    public Writer(@NonNull OutputStreamWriter stream) {
        super(stream);
    }

    @SneakyThrows
    public void write(String string) {
        if (string.equals(this.lineSeparator)) {
            if (this.lastWritten != null)
                super.write(string);

        } else {
            this.newLine();
            this.lastWritten = string;
            super.write(string);
        }
    }

    @SneakyThrows
    public void newLine() {
        super.newLine();
    }

    @SneakyThrows
    public void end() {
        flush();
        close();
    }
}
