package com.oop.orangeengine.message;

import org.bukkit.command.CommandSender;

import java.util.Map;

public interface Sendable {
    void send(CommandSender ...receivers);

    void send(Map<String, Object> placeholders, CommandSender ...receivers);
}
