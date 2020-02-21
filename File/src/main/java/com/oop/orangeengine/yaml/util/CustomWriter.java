package com.oop.orangeengine.yaml.util;

import sun.security.action.GetPropertyAction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.AccessController;

public class CustomWriter extends BufferedWriter {
    private final String lineSeparator = (String) AccessController.doPrivileged(new GetPropertyAction("line.separator"));
    private String lastWritten = null;

    public CustomWriter(Writer writer) {
        super(writer);
    }

    public void write(String s, int i, int i1) throws IOException {
        if (s.equals(this.lineSeparator)) {
            if (this.lastWritten != null) {
                super.write(s, i, i1);
            }
        } else {
            this.newLine();
            this.lastWritten = s;
            super.write(s, i, i1);
        }
    }

    public void newLine() throws IOException {
        super.newLine();
    }

    public String getLineSeparator() {
        return this.lineSeparator;
    }

    public String getLastWritten() {
        return this.lastWritten;
    }

    public void setLastWritten(String lastWritten) {
        this.lastWritten = lastWritten;
    }
}