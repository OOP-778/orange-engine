package com.oop.orangeengine.message.additions;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public abstract class AAddition implements Cloneable {

    public abstract void apply(TextComponent component);

    public void onSend(Player player) {
    }

    @Override
    public AAddition clone() {
        try {
            return ((AAddition) super.clone());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void replace(String key, Object obj) {}

}
