package com.oop.orangeEngine.command.arg.arguments;

import com.oop.orangeEngine.command.OOPCommand;
import com.oop.orangeEngine.command.arg.CommandArgument;
import com.oop.orangeEngine.main.Helper;
import com.oop.orangeEngine.main.util.pair.OPair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class PlayerArg extends CommandArgument<Player> {

    public PlayerArg() {
        setDescription("An online player");
        setIdentity("player");
        setMapper(input -> {

            Player player = Bukkit.getPlayer(input);
            return new OPair<>(player, player == null ? "Failed to find online player by name " + input : "");

        });
    }

    @Override
    public void onAdd(OOPCommand command) {
        command.nextTabComplete((args) -> Helper.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
    }
}
