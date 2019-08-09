package com.oop.orangeEngine.command;

import com.oop.orangeEngine.command.arg.CommandArgument;
import com.oop.orangeEngine.command.req.RequirementMapper;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

@Getter
public class OOPCommand {

    private Class[] ableToExecute = new Class[]{Player.class, ConsoleCommandSender.class};

    //The name of the command
    private String label;

    //The description of the command
    private String description = "";

    //If command will be shown when list of commands are shown / on tab complete
    private boolean secret = false;

    //Sender requirements
    private Set<RequirementMapper> requirementSet = new HashSet<>();

    //Aliases of the command
    private Set<String> aliases = new HashSet<>();

    //Permission of the command / No permission message
    private String permission = "none", noPermissionMessage = "&c&l(!)&7 You don't have permission to use this command!";

    //Sub commands
    private Map<String, OOPCommand> subCommands = new HashMap<>();

    //Args
    private Map<String, CommandArgument> argumentMap = new LinkedHashMap<>();

    //Listener
    private Consumer<WrappedCommand> listener;

    //Tab completion
    private Map<Integer, TabCompletion> tabComplete = new HashMap<>();

    //Parent
    private OOPCommand parent;

    //for nextTabComplete
    private int currentTabComplete = 0;

    public OOPCommand label(String label) {
        this.label = label;
        return this;
    }

    public OOPCommand description(String description) {
        this.description = description;
        return this;
    }

    public OOPCommand secret(boolean secret) {
        this.secret = secret;
        return this;
    }

    public OOPCommand addRequirement(RequirementMapper mapper) {
        this.requirementSet.add(mapper);
        return this;
    }

    public OOPCommand alias(String alias) {
        this.aliases.add(alias);
        return this;
    }

    public OOPCommand permission(String permission) {
        this.permission = permission;
        return this;
    }

    public OOPCommand noPermissionMessage(String noPermissionMessage) {
        this.noPermissionMessage = noPermissionMessage;
        return this;
    }

    public OOPCommand subCommand(OOPCommand command) {
        this.subCommands.put(command.getLabel(), command.parent(this));
        return this;
    }

    public OOPCommand argument(CommandArgument argument) {
        argumentMap.put(argument.getIdentifier(), argument);
        argument.onAdd(this);
        return this;
    }

    public OOPCommand listen(Consumer<WrappedCommand> listener) {
        this.listener = listener;
        return this;
    }

    public OOPCommand tabComplete(Integer place, TabCompletion completion) {
        this.tabComplete.put(place, completion);
        return this;
    }

    public OOPCommand nextTabComplete(TabCompletion completion) {
        currentTabComplete++;
        this.tabComplete.put(currentTabComplete, completion);
        return this;
    }

    public OOPCommand setAbleToExecute(Class<? extends CommandSender>... ableToExecute) {
        this.ableToExecute = ableToExecute;
        return this;
    }

    public OOPCommand parent() {
        return parent;
    }

    public OOPCommand parent(OOPCommand parent) {
        this.parent = parent;
        return this;
    }

    public OOPCommand subCommand(String arg) {
        return subCommands.values().stream().
                filter(cmd -> cmd.getLabel().equalsIgnoreCase(arg) || cmd.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(arg))).
                findFirst().
                orElse(null);
    }

    public boolean isSubCommand(String arg) {
        return subCommand(arg) != null;
    }

    public String getLabelWithParents() {
        return getLabelWithParents("");
    }

    private String getLabelWithParents(String current) {

        if (getParent() == null)
            return current + getLabel() + " ";
        else
            return getParent().getLabelWithParents(current + getLabel() + " ");

    }

}
