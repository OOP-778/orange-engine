package com.oop.orangeengine.command;

import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.command.req.RequirementMapper;
import com.oop.orangeengine.main.Helper;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

@Getter
public class OCommand {

    private Class[] ableToExecute = new Class[]{Player.class, ConsoleCommandSender.class};

    private String notAbleToExecuteMessage = "&c&l(!)&7 Command can only be executed by %sender%";

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
    private Map<String, OCommand> subCommands = new HashMap<>();

    //Args
    private Map<String, CommandArgument> argumentMap = new LinkedHashMap<>();

    //Listener
    private Consumer<WrappedCommand> listener;

    //Tab completion
    private Map<Integer, TabCompletion> tabComplete = new HashMap<>();

    //Parent
    private OCommand parent = null;

    //for nextTabComplete
    private int currentTabComplete = 0;

    public OCommand label(String label) {
        this.label = label;
        return this;
    }

    public OCommand description(String description) {
        this.description = description;
        return this;
    }

    public OCommand secret(boolean secret) {
        this.secret = secret;
        return this;
    }

    public OCommand addRequirement(RequirementMapper mapper) {
        this.requirementSet.add(mapper);
        return this;
    }

    public OCommand alias(String... alias) {
        this.aliases.addAll(Arrays.asList(alias));
        return this;
    }

    public OCommand permission(String permission) {
        this.permission = permission;
        return this;
    }

    public OCommand noPermissionMessage(String noPermissionMessage) {
        this.noPermissionMessage = noPermissionMessage;
        return this;
    }

    public OCommand subCommand(OCommand command) {
        this.subCommands.put(command.getLabel(), command.setParent(this));
        return this;
    }

    public OCommand argument(CommandArgument argument) {
        argumentMap.put(argument.getIdentifier(), argument);
        argument.onAdd(this);
        return this;
    }

    public OCommand listen(Consumer<WrappedCommand> listener) {
        this.listener = listener;
        return this;
    }

    public OCommand tabComplete(Integer place, TabCompletion completion) {
        this.tabComplete.put(place, completion);
        return this;
    }

    public OCommand nextTabComplete(TabCompletion completion) {
        currentTabComplete++;
        this.tabComplete.put(currentTabComplete, completion);
        return this;
    }

    public OCommand ableToExecute(Class<? extends CommandSender>... ableToExecute) {
        this.ableToExecute = ableToExecute;
        return this;
    }

    public OCommand getParent() {
        return parent;
    }

    public OCommand setParent(OCommand parent) {
        this.parent = parent;
        return this;
    }

    public OCommand subCommand(String arg) {
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

        Helper.print(getParent().getLabel());
        if (getParent() == null)
            return current + getLabel() + " ";
        else
            return getParent().getLabelWithParents(current + getLabel() + " ");

    }

}