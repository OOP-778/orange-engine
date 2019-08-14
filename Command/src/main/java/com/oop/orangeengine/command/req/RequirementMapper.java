package com.oop.orangeengine.command.req;

import com.oop.orangeengine.main.util.pair.OPair;
import org.bukkit.command.CommandSender;

public interface RequirementMapper {

    OPair<Boolean, String> accepts(CommandSender sender);

}
