package com.oop.orangeengine.command.arg.arguments;

import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;
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
    public void onAdd(OCommand command) {
        command.nextTabComplete((previous, args) -> Helper.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
    }
}
