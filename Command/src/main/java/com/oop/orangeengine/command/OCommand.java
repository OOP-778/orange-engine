package com.oop.orangeengine.command;

import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.command.req.RequirementMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Getter
public class OCommand implements Cloneable {
    private Class[] ableToExecute = new Class[]{Player.class, ConsoleCommandSender.class};

    private String notAbleToExecuteMessage = "&c&l(!)&7 Command can only be executed by %sender%";

    //The name of the command
    private String label;

    //The description of the command
    private String description = null;

    //If command will be shown when list of commands are shown / on tab complete
    private boolean secret = false;

    //Sender requirements
    private Set<RequirementMapper> requirementSet = new HashSet<>();

    //Aliases of the command
    private Set<String> aliases = new HashSet<>();

    //Permission of the command / No permission message
    private String permission = null;

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

    // For registered Bukkit Command
    @Setter
    private Command registeredCommand;

    // For scheme cache
    private Map<String, List<TextComponent>> schemeCache = new HashMap<>();

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

    public OCommand subCommand(OCommand command) {
        this.subCommands.put(command.getLabel(), command.setParent(this));
        return this;
    }

    public OCommand argument(CommandArgument argument) {
        argumentMap.put(argument.getIdentity(), argument);
        currentTabComplete++;
        argument.onAdd(this);
        return this;
    }

    @Deprecated
    public OCommand listen(Consumer<WrappedCommand> listener) {
        this.listener = listener;
        return this;
    }

    public OCommand tabComplete(Integer place, TabCompletion completion) {
        this.tabComplete.put(place, completion);
        return this;
    }

    public OCommand nextTabComplete(TabCompletion completion) {
        this.tabComplete.put(currentTabComplete, completion);
        return this;
    }

    @SafeVarargs
    public final OCommand ableToExecute(Class<? extends CommandSender>... ableToExecute) {
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
        return getLabelWithParents(" ");
    }

    public String getLabelWithParents(String connector) {
        String labelWithParents = getLabelWithParents("", connector);

        List<String> labels;
        if (labelWithParents.contains(connector))
            labels = new ArrayList<>(Arrays.asList(labelWithParents.split(Pattern.quote(connector))));
        else
            return labelWithParents;

        Collections.reverse(labels);
        return String.join(connector, labels);
    }

    private String getLabelWithParents(String current, String connector) {
        if (getParent() == null)
            return current + getLabel() + connector;
        else
            return getParent().getLabelWithParents(current + getLabel() + connector, connector);
    }

    public OCommand onCommand(Consumer<WrappedCommand> consumer) {
        this.listener = consumer;
        return this;
    }

    public CommandArgument argument(String name) {
        return argumentMap.get(name);
    }

    public <T> CommandArgument argument(Class<T> type) {
        return getArgumentMap().values()
                .stream()
                .filter(argument -> argument.getClass() == type)
                .findFirst()
                .orElse(null);
    }

    @SneakyThrows
    public OCommand clone() {
        OCommand command = (OCommand) super.clone();
        command.aliases = new HashSet<>(command.aliases);

        command.subCommands = new HashMap<>();
        for (OCommand value : getSubCommands().values()) {
            OCommand clone = value.clone();
            command.subCommand(clone);
        }

        return command;
    }
}
