package com.oop.testingPlugin;

import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.yaml.OConfiguration;

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
