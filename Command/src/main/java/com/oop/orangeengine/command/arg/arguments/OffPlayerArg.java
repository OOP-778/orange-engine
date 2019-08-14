package com.oop.orangeengine.command.arg.arguments;

import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.pair.OPair;
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
    public void onAdd(OCommand command) {
        command.nextTabComplete((args) -> Helper.getOfflinePlayers().stream().map(OfflinePlayer::getName).collect(Collectors.toList()));
    }
}