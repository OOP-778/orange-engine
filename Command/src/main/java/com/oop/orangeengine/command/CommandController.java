package com.oop.orangeengine.command;

import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.command.req.RequirementMapper;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.plugin.OComponent;
import com.oop.orangeengine.main.util.OSimpleReflection;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.line.LineContent;
import com.oop.orangeengine.message.line.MessageLine;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;

import static com.oop.orangeengine.main.Engine.getEngine;

public class CommandController {

    private Set<Command> registered = new HashSet<>();
    private CommandMap commandMap;

    private ColorScheme colorScheme;
    private JavaPlugin plugin;

    public CommandController(EnginePlugin plugin) {
        this.plugin = plugin;
        this.colorScheme = new ColorScheme();
        try {

            Field cMap = SimplePluginManager.class.getDeclaredField("commandMap");
            cMap.setAccessible(true);
            commandMap = (CommandMap) cMap.get(Bukkit.getPluginManager());

        } catch (Throwable thrw) {
            throw new IllegalStateException("Failed to initialize CommandMap", thrw);
        }
        plugin.onDisable(this::unregisterAll);
    }

    public void unregisterAll() {
        try {
            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);

            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            for (Command command : registered) {
                knownCommands.remove(command.getName());
                command.unregister(commandMap);

                getEngine().getLogger().printDebug("Unregistered " + command.getName() + " / " + command.getLabel());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not unregister commands", e);
        }
    }

    public void register(OCommand command) {
        Command bukkitCommand = new Command(command.getLabel(), command.getDescription(), "none", new ArrayList<>(command.getAliases())) {
            @Override
            public boolean execute(CommandSender sender, String cmdName, String[] args) {

                if (args.length == 0)
                    handleCommand(args, sender, command);

                else {

                    if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {

                        handleProperUsage(command, sender);
                        return true;

                    }
                    String secondArg = args[0];
                    String subCommandName = command.getSubCommands().keySet().stream().filter(subC -> subC.equalsIgnoreCase(secondArg)).findFirst().orElse(null);
                    if (subCommandName != null)
                        handleCommand(cutArray(args, 1), sender, command.getSubCommands().get(subCommandName));

                    else {

                        if (command.getListener() == null) {
                            sender.sendMessage(colorize("&cSub Command by name " + secondArg + " not found!"));
                            handleProperUsage(command, sender);

                        } else
                            handleCommand(args, sender, command);

                    }

                }
                return true;
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
                List<String> completion = new ArrayList<>();
                if (command.getSubCommands().isEmpty())
                    completion.addAll(handleTabComplete(command, args));

                else if (args.length == 1) {
                    command.getSubCommands().values()
                            .stream()
                            .filter(OCommand::isSecret)
                            .filter(c -> !c.getPermission().equalsIgnoreCase("NONE") && sender.hasPermission(c.getPermission()))
                            .filter(c -> args[0].trim().length() != 0 && c.getLabel().startsWith(args[0]))
                            .forEach(command -> completion.add(command.getLabel()));

                } else
                    completion.addAll(handleTabComplete(command.subCommand(args[0]), cutArray(args, 1)));

                return completion;
            }
        };
        registered.add(bukkitCommand);
        commandMap.register(plugin.getName(), bukkitCommand);

    }

    public List<String> handleTabComplete(OCommand command, String[] args) {
        List<String> completion = new ArrayList<>();
        if (command.getTabComplete().containsKey(args.length)) {
            Collection<String> completions = command.getTabComplete().get(args.length).handleTabCompletion(args);
            if (args.length > 0 && args[0].trim().length() != 0)
                completions.removeIf(c -> !c.contains(args[0]));
        }

        return completion;
    }

    private String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    private void handleCommand(String[] args, CommandSender sender, OCommand command) {

        //Check for permission
        if (!command.getPermission().equalsIgnoreCase("none") && !sender.hasPermission(command.getPermission())) {

            sender.sendMessage(colorize(command.getNoPermissionMessage()));
            return;

        }

        //Check for ableToExecute
        int found = 0;
        for (Class executerClass : command.getAbleToExecute())
            if(executerClass.isAssignableFrom(sender.getClass())) found++;

        if(found == 0) {

            sender.sendMessage(colorize(command.getNotAbleToExecuteMessage().replace("%sender%", sender instanceof Player ? "Console" : "Player")));
            return;

        }

        //Check for sender requirements
        for (RequirementMapper requirementMapper : command.getRequirementSet()) {

            OPair<Boolean, String> mapped = requirementMapper.accepts(sender);
            if (!mapped.getFirst()) {

                sender.sendMessage(colorize(mapped.getSecond()));
                return;

            }
        }

        if (args.length >= 1 && command.isSubCommand(args[0])) {
            handleCommand(cutArray(args, 1), sender, command.subCommand(args[0]));
            return;
        } else if (command.getListener() == null) {
            handleProperUsage(command, sender);
            return;
        }

        Map<String, Object> arguments = new HashMap<>();

        if (command.getArgumentMap().isEmpty())
            execCommand(new WrappedCommand(sender, arguments), command);
        else {

            String[] argsCopy = args.clone();
            List<CommandArgument> commandArguments = new ArrayList<>(command.getArgumentMap().values());

            for (CommandArgument arg : commandArguments) {
                if (argsCopy.length == 0) {

                    if (!arg.isRequired()) continue;
                    handleProperUsage(command, sender);
                    return;

                }

                String stringValue = argsCopy[0];
                if (arg.getMapper() != null) {

                    OPair<Object, String> value = arg.getMapper().product(stringValue);
                    if (value.getFirst() == null) {

                        sender.sendMessage(colorize("&c&l* &7Error: &c" + value.getSecond()));
                        handleProperUsage(command, sender);
                        return;

                    } else {

                        arguments.put(arg.getIdentity(), value.getFirst());
                        argsCopy = cutArray(argsCopy, 1);

                    }
                }

                if (arg.isGrabAllNextArgs()){
                    String current = (String) arguments.get(arg.getIdentity());
                    for (String nextArg : argsCopy) {
                        current += " " + nextArg;
                    }

                    arguments.remove(arg.getIdentity());
                    arguments.put(arg.getIdentity(), current);

                    break;
                }

            }

            execCommand(new WrappedCommand(sender, arguments), command);
        }
    }

    private void handleProperUsage(OCommand command, CommandSender sender) {

        //Check if simple message is required
        if (command.getSubCommands().isEmpty()) {
            OMessage message = new OMessage();
            MessageLine line = new MessageLine();
            String allParents = command.getLabelWithParents();

            line.append(colorScheme.getMainColor() + "Usage: /");

            LineContent labelContent = new LineContent(reverseLabel(allParents.substring(0, allParents.length() - 1)));
            if (!command.getDescription().equalsIgnoreCase("none"))
                labelContent.hoverText(colorScheme.getMainColor() + command.getDescription());
            line.append(labelContent);

            buildArgs(command.getArgumentMap().values(), line);
            message.appendLine(line);
            if (!command.getPermission().equalsIgnoreCase("none")) {
                message.appendLine(colorScheme.getMainColor() + "&l* " + colorScheme.getMainColor() + "Permission: &f" + command.getPermission());
            }
            if (!command.getDescription().equalsIgnoreCase("none")) {
                message.appendLine(colorScheme.getMainColor() + "&l* " + colorScheme.getMainColor() + "Description: &f" + command.getDescription());
            }

            if (sender instanceof Player)
                message.send(((Player) sender));

        } else {

            boolean hasRequired = command.getSubCommands().values().stream().
                    map(cmd -> (int) cmd.getArgumentMap().values().stream().filter(CommandArgument::isRequired).count()).
                    anyMatch(c -> c > 0);
            boolean hasOptional = command.getSubCommands().values().stream().
                    map(cmd -> (int) cmd.getArgumentMap().values().stream().filter(a -> !a.isRequired()).count()).
                    anyMatch(c -> c > 0);

            //Advanced
            OMessage message = new OMessage();
            message.appendLine(colorScheme.getSecondColor() + "&l---====" + colorScheme.getMainColor() + " " + StringUtils.capitalize(command.getLabel()) + " Help");

            //Format Builder
            if (hasOptional && hasRequired)
                message.appendLine(colorScheme.getSecondColor() + "   <> - Required, [] - Optional");

            else if (hasRequired)
                message.appendLine(colorScheme.getSecondColor() + "   <> - Required");

            else if (hasOptional)
                message.appendLine(colorScheme.getSecondColor() + "   [] - Optional");

            message.appendLine(" ");

            //Check if main command has stuff :D
            if(command.getListener() != null) {
                MessageLine line = new MessageLine();

                String allParents = command.getLabelWithParents();
                LineContent labelContent = new LineContent(colorScheme.getMainColor() + "/" + reverseLabel(allParents.substring(0, allParents.length() - 1)));
                if (!command.getDescription().equalsIgnoreCase("None"))
                    labelContent.hoverText(colorScheme.getMainColor() + command.getDescription());
                line.append(labelContent);

                buildArgs(command.getArgumentMap().values(), line);
                if (!command.getDescription().equalsIgnoreCase("None"))
                    line.append(colorScheme.getMainColor() + " - " + command.getDescription());

                message.appendLine(line);
            }

            for (OCommand subCommand : command.getSubCommands().values()) {

                //Check if the command is hidden
                if (subCommand.isSecret())
                    continue;

                //Check for permission (if sender has the permission to use this command)
                if (subCommand.getPermission().equalsIgnoreCase("none") && !sender.hasPermission(subCommand.getPermission()))
                    continue;

                MessageLine line = new MessageLine();
                line.append(colorScheme.getMainColor() + "/" + reverseLabel(command.getLabelWithParents()));

                LineContent labelContent = new LineContent(colorScheme.getMainColor() + subCommand.getLabel());
                if (!subCommand.getDescription().equalsIgnoreCase("None"))
                    labelContent.hoverText(colorScheme.getMainColor() + subCommand.getDescription());
                line.append(labelContent);

                buildArgs(subCommand.getArgumentMap().values(), line);
                if (!subCommand.getDescription().equalsIgnoreCase("None"))
                    line.append(colorScheme.getMainColor() + " - " + subCommand.getDescription());

                message.appendLine(line);

            }
            if (sender instanceof Player)
                message.send(((Player) sender));

        }

    }

    private String reverseLabel(String labelWithParents) {

        List<String> strings = new ArrayList<>();
        if (labelWithParents.contains(" "))
            strings.addAll(Arrays.asList(labelWithParents.split(" ")));
        else
            return labelWithParents;

        Collections.reverse(strings);

        StringBuffer buffer = new StringBuffer();
        for (String append : strings) {
            buffer.append(append + " ");
        }

        return buffer.toString();
    }

    private void execCommand(WrappedCommand command, OCommand oopCommand) {
        oopCommand.getListener().accept(command);
    }

    private String[] cutArray(String[] array, int amount) {
        if (array.length <= amount)
            return new String[0];
        else
            return Arrays.copyOfRange(array, amount, array.length);
    }

    public void properUsage(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    private void buildArgs(Collection<CommandArgument> args, MessageLine line) {

        //Format = <required> [optional]
        if (args.stream().anyMatch(CommandArgument::isRequired)) {

            args.stream().filter(CommandArgument::isRequired).forEach(arg -> {

                line.append(" <");
                LineContent content = new LineContent("&f" + arg.getIdentity()).
                        hoverText(colorScheme.getMainColor() + arg.getDescription());
                line.append(content);
                line.append(colorScheme.getMainColor() + ">");

            });
            line.append(colorScheme.getMainColor());

        }

        if (args.stream().anyMatch(a -> !a.isRequired())) {

            line.append(colorScheme.getSecondColor()).append(" ");
            args.stream().filter(a -> !a.isRequired()).forEach(arg -> {

                line.append("[");
                LineContent content = new LineContent("&f" + arg.getIdentity()).
                        hoverText(colorScheme.getSecondColor() + arg.getDescription());
                line.append(content);
                line.append(colorScheme.getSecondColor() + "]");

            });
            line.append(colorScheme.getMarkupColor());

        }

    }

}
