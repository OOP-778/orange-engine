package com.oop.orangeengine.message.additions.action;

import com.oop.orangeengine.message.additions.AAddition;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ConsumerAddition extends AAddition {
    @Override
    public void apply(TextComponent component) {

    }

    @Override
    public void onSend(Player player) {
        super.onSend(player);
    }
}
