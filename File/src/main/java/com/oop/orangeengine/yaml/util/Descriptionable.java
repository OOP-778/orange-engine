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
        this.description = new ArrayList<>(description);
        description.clear();
        return this;
    }

    public List<String> description() {
        return description;
    }

    public void writeDescription(CustomWriter bw, int spaces) throws IOException {

        if (!description.isEmpty()) {

            bw.newLine();
            if (description.size() > 1) {
                bw.write(ConfigurationUtil.stringWithSpaces(spaces) + "#------------------");
                bw.newLine();

                for (String d : description) {

                    if (d.trim().length() > 0) {
                        bw.write(ConfigurationUtil.stringWithSpaces(spaces) + "# " + d);
                    }

                }

                bw.newLine();
                bw.write(ConfigurationUtil.stringWithSpaces(spaces) + "#------------------");
            } else {

                for (String d : description)
                    bw.write(ConfigurationUtil.stringWithSpaces(spaces) + "# " + d);
            }

        }

    }
}
