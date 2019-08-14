package com.oop.orangeengine.yaml.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Descriptionable {

    private List<String> description = new ArrayList<>();

    public Descriptionable addDescription(String string) {
        description.add(string);
        return this;
    }

    public Descriptionable description(List<String> description) {
        this.description = description.stream().collect(Collectors.toList());
        description.clear();
        return this;
    }

    public List<String> description() {
        return description;
    }

    public void writeDescription(CustomWriter bw, int spaces) throws IOException {

        if (!description.isEmpty()) {

            if (description.size() > 1) {
                bw.newLine();
                bw.writeWithoutSmart(ConfigurationUtil.stringWithSpaces(spaces) + "#------------------");
                bw.newLine();

                for (String d : description) {

                    if (d.trim().length() > 0) {
                        bw.newLine();
                        bw.writeWithoutSmart(ConfigurationUtil.stringWithSpaces(spaces) + '#' + d);
                    }

                }

                bw.newLine();
                bw.newLine();
                bw.writeWithoutSmart(ConfigurationUtil.stringWithSpaces(spaces) + "#------------------");
            } else {

                for (String d : description) {

                    bw.newLine();
                    bw.writeWithoutSmart(ConfigurationUtil.stringWithSpaces(spaces) + '#' + d);

                }
            }
            bw.newLine();

        }

    }
}
