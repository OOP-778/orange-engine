package com.oop.orangeengine.message.additions.action.secretCmd;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public class SecretCommand {

    private long timeout;
    private Predicate<SecretCommand> disposeWhen;
    private Consumer<Player> run;

}
