package com.oop.testingPlugin;

import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.arguments.OffPlayerArg;
import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.plugin.EnginePlugin;

public class TestingPlugin extends EnginePlugin {

    @Override
    public void enable() {

        OFile file = new OFile(getDataFolder(), "messages.yml");
        file.createIfNotExists();

        CommandController commandController = new CommandController(this);
        commandController.register(
                new OCommand()
                        .label("create")
                        .description("Testing Command")
                        .argument(new OffPlayerArg().setIsRequired(true))
                        .listen(command -> {

                        })
        );


    }

    public class TestObject {

    }


}
