package com.oop.testingPlugin;

import com.oop.orangeEngine.file.OFile;
import com.oop.orangeEngine.main.plugin.EnginePlugin;
import com.oop.orangeEngine.yaml.OConfiguration;

public class TestingPlugin extends EnginePlugin {

    @Override
    public void enable() {

        OFile file = new OFile(getDataFolder(), "messages.yml");
        file.createIfNotExists();

        OConfiguration configuration = new OConfiguration(file.getFile());


    }

    public class TestObject {

    }


}
