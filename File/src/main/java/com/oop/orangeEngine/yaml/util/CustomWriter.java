package com.oop.orangeEngine.yaml.util;

import lombok.Getter;
import lombok.Setter;
import sun.security.action.GetPropertyAction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.AccessController;

@Getter
public class CustomWriter extends BufferedWriter {

    @Setter
    private String lastWritten = "";

    public CustomWriter(Writer writer) {
        super(writer);
    }

    @Override
    public void write(String s, int i, int i1) throws IOException {

        super.write(s, i, i1);
        if (s.equalsIgnoreCase(AccessController.doPrivileged(new GetPropertyAction("line.separator"))))
            return;

        newLine();

        setLastWritten(s);
        ConfigurationUtil.smartNewLine(this);
    }

    public void writeWithoutSmart(String s) throws IOException {
        super.write(s, 0, s.length());
    }
}
