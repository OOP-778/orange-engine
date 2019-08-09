package com.oop.orangeEngine.command.req;

import org.apache.commons.math3.util.Pair;
import org.bukkit.command.CommandSender;

public interface RequirementMapper {

    Pair<Boolean, String> accepts(CommandSender sender);

}
