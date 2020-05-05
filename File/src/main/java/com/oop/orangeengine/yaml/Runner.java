package com.oop.orangeengine.yaml;

import java.io.File;

public class Runner {

    public static void main(String[] args) {
        String path = Runner.class.getClassLoader().getResource("testing.yml").getPath();
        System.out.println("Configuration path: " + path);

        File file = new File(path);
        Config configuration = new Config(file);
        configuration.save();
    }
}
