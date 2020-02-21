package com.oop.orangeengine.message;

import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.yaml.OConfiguration;

import java.io.File;

public class Tester {

    public static void main(String[] args) {
        OConfiguration configuration = new OConfiguration(new OFile(new File("folder"), "test.yml").createIfNotExists());
        YamlMessage.save(new OMessage().appendLine("wagawgawgaw"), "test", configuration, "wagawgawg");
        configuration.save();

    }

}
