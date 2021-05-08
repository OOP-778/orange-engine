package com.oop.orangeengine.command;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class CommandsRegisterer {
    private Set<OCommand> commands = new HashSet<>();

    private CommandController controller;
    public CommandsRegisterer(CommandController controller) {
        this.controller = controller;
    }

    public CommandsRegisterer add(OCommand command) {
        commands.add(command);
        return this;
    }

    public CommandsRegisterer use(Consumer<Set<OCommand>> useCmds) {
        useCmds.accept(commands);
        return this;
    }

    public CommandsRegisterer remap() {
        CommandRemapper remapper = new CommandRemapper();
        remapper.updateConfig(commands.toArray(new OCommand[0]));
        remapper.remap(commands.toArray(new OCommand[0]));
        return this;
    }

    public void push() {
        commands.forEach(cmd -> controller.register(cmd));
    }
}
