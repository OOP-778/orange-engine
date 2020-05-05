package com.oop.orangeengine.command;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.message.OMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class WrappedCommand {

    @Getter
    private CommandSender sender;
    private Map<String, Object> arguments;

    public WrappedCommand(CommandSender sender, Map<String, Object> arguments) {
        this.sender = sender;
        this.arguments = arguments;
    }

    public Player getSenderAsPlayer() {
        return (Player) sender;
    }

    public <T> Optional<T> getArg(String arg) {
        return Optional.ofNullable((T) arguments.get(arg));
    }

    public <T> Optional<T> getArg(String arg, Class<T> type) {
        return Optional.ofNullable((T) arguments.get(arg));
    }

    public <T> T getArgAsReq(String arg) {
        return (T) arguments.get(arg);
    }

    public <T> T getArgAsReq(String arg, Class<T> type) {
        return type.cast(arguments.get(arg));
    }

    public void sendMessage(OMessage message, Map<String, String> placeholders) {
        message = message.clone();
        message.replace(placeholders);
        message.send(getSender());
    }
}
