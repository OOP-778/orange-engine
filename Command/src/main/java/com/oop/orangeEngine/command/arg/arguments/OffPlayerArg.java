package com.oop.orangeEngine.command.arg.arguments;

import com.oop.orangeEngine.command.OOPCommand;
import com.oop.orangeEngine.command.arg.CommandArgument;
import com.oop.orangeEngine.main.Helper;
import com.oop.orangeEngine.main.util.pair.OPair;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.stream.Collectors;

public class OffPlayerArg extends CommandArgument<OfflinePlayer> {

    public OffPlayerArg() {
        setDescription("An offline player");
        setIdentity("player");
        setMapper(input -> {

            OfflinePlayer player = Bukkit.getOfflinePlayer(input);
            return new OPair<>(player, player == null ? "Failed to find offline player by name " + input : "");

        });
    }

    @Override
    public void onAdd(OOPCommand command) {
        command.nextTabComplete((args) -> Helper.getOfflinePlayers().stream().map(OfflinePlayer::getName).collect(Collectors.toList()));
    }
}