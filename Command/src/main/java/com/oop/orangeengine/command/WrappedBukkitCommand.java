package com.oop.orangeengine.command;

import org.bukkit.command.Command;

import java.util.List;

public abstract class WrappedBukkitCommand extends Command {
    protected WrappedBukkitCommand(String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }
}
