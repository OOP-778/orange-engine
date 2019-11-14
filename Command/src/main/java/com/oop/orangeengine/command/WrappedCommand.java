package com.oop.orangeengine.command;

import com.oop.orangeengine.main.util.OptionalConsumer;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

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

    public OptionalConsumer<Object> getArg(String arg) {
        return OptionalConsumer.of(Optional.ofNullable(arguments.get(arg)));
    }

    public <T> T getArgAsReq(String arg) {
        return (T) arguments.get(arg);
    }

    public <T> T getArgAsReq(String arg, Class<T> type) {
        return type.cast(arguments.get(arg));
    }
}
