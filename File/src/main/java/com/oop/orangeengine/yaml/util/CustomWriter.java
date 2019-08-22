package com.oop.orangeengine.yaml.util;

import lombok.Getter;
import lombok.Setter;
import sun.security.action.GetPropertyAction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.AccessController;

@Getter
public class CustomWriter extends BufferedWriter {

    final private String lineSeparator = AccessController.doPrivileged(new GetPropertyAction("line.separator"));

    @Setter
    private String lastWritten = null;

    public CustomWriter(Writer writer) {
        super(writer);
    }

    @Override
    public void write(String s, int i, int i1) throws IOException {
        if(s.equals(lineSeparator)) {
            if(lastWritten == null)
                return;
            super.write(s, i, i1);
            return;
        }

        newLine();
        lastWritten = s;
        super.write(s, i, i1);
    }

    @Override
    public void newLine() throws IOException {
        super.newLine();
    }
}
