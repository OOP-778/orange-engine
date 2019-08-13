package com.oop.testingPlugin;

import com.oop.orangeEngine.file.OFile;
import com.oop.orangeEngine.main.plugin.EnginePlugin;
import com.oop.orangeEngine.yaml.ConfigurationSection;
import com.oop.orangeEngine.yaml.OConfiguration;
import com.oop.orangeEngine.yaml.mapper.section.ConfigurationSerializable;

public class TestingPlugin extends EnginePlugin {

    @Override
    public void enable() {

        OFile file = new OFile(getDataFolder(), "messages.yml");
        file.createIfNotExists();

        OConfiguration.registerDefaultSerializer(TestObjectSerializer.class);
        OConfiguration configuration = new OConfiguration(file.getFile());


    }

    public class TestObject {

    }


    public class TestObjectSerializer implements ConfigurationSerializable<TestObject> {

        @Override
        public void save(ConfigurationSection section, TestObject object) {

        }

        @Override
        public TestObject load(ConfigurationSection section) {
            return null;
        }

        @Override
        public String getType() {
            return "testObject";
        }
    }

}
