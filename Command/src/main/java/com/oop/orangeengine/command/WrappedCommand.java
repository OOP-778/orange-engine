package com.oop.orangeengine.command;

import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.OMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        if (sender instanceof Player)
            message.send(getSenderAsPlayer(), placeholders);

        else {
            message = message.clone();
            List<String> raw = new ArrayList<>();
            message.getLineList().forEach(line -> {
                String[] array = new String[]{line.getRaw()};
                placeholders.forEach((k, v) -> array[0] = array[0].replace(k, v));
                raw.add(array[0]);
            });
            raw.forEach(line -> Bukkit.getConsoleSender().sendMessage(Helper.color(line)));
        }
    }
}
