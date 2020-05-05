package com.oop.orangeengine.command;

import com.google.common.collect.Lists;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.command.req.RequirementMapper;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.message.impl.chat.ChatLine;
import com.oop.orangeengine.message.impl.chat.LineContent;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.oop.orangeengine.main.Engine.getEngine;

public class CommandController {

    private Set<Command> registered = new HashSet<>();
    private CommandMap commandMap;

    @Setter
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
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
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
                if (command.getSubCommands().isEmpty()) {
                    completion.addAll(handleTabComplete(command, args, sender));

                } else if (args.length == 1) {
                    completion.addAll(getSubCommandsFor(command, sender));
                    if (args[0].trim().length() > 0)
                        completion.removeIf(commandName -> !commandName.toLowerCase().startsWith(args[0].toLowerCase()));

                } else if (args.length > 1) {
                    OCommand subCommand = command.subCommand(args[0]);
                    if (subCommand == null) return completion;

                    completion.addAll(handleTabComplete(subCommand, cutArray(args, 1), sender));
                }
                return completion;
            }
        };

        registered.add(bukkitCommand);
        commandMap.register(plugin.getName(), bukkitCommand);
    }

    public Collection<String> getSubCommandsFor(OCommand command, CommandSender sender) {
        return command.getSubCommands().values()
                .stream()
                .filter(subCommand -> !subCommand.isSecret())
                .filter(subCommand -> subCommand.getPermission().equalsIgnoreCase("NONE") || sender.hasPermission(subCommand.getPermission()))
                .map(OCommand::getLabel)
                .collect(Collectors.toList());
    }

    public List<String> handleTabComplete(@NonNull OCommand command, String[] args, CommandSender sender) {
        List<String> completion = new ArrayList<>();
        if (args.length >= 1) {
            OCommand subCommand = command.subCommand(args[0]);
            if (subCommand != null) {
                completion.addAll(handleTabComplete(subCommand, cutArray(args, 1), sender));
                return completion;
            }
        }

        if (!command.getSubCommands().isEmpty())
            completion.addAll(getSubCommandsFor(command, sender));

        if (completion.isEmpty()) {
            List<Object> objects = Lists.newArrayList();
            List<CommandArgument> arguments = new ArrayList<>(command.getArgumentMap().values());
            for (int i = 0; i < args.length-1; i++) {
                if ((arguments.size() - 1) <= i)
                    break;

                CommandArgument commandArgument = arguments.get(i);
                Object value = commandArgument.getMapper().product(args[i]).getKey();
                if (value != null)
                    objects.add(value);
            }

            TabCompletion tabCompletion = command.getTabComplete().get(args.length);
            if (tabCompletion != null)
                completion.addAll(tabCompletion.handleTabCompletion(new CompletionResult(objects), args));
        }

        if (args.length >= 1) {
            String lastArg = args[args.length - 1];
            if (lastArg.trim().length() > 0)
                completion.removeIf(name -> !name.toLowerCase().startsWith(args[0].toLowerCase()));
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
            OChatMessage message = new OChatMessage();
            ChatLine line = new ChatLine();
            String allParents = command.getLabelWithParents();

            line.append(colorScheme.getMainColor() + "Usage: /");

            LineContent labelContent = new LineContent(reverseLabel(allParents.substring(0, allParents.length() - 1)));
            if (!command.getDescription().equalsIgnoreCase("none"))
                labelContent.hover().add(colorScheme.getMainColor() + command.getDescription());
            line.append(labelContent);

            buildArgs(command.getArgumentMap().values(), line);
            message.append(line);
            if (!command.getPermission().equalsIgnoreCase("none")) {
                message.append(colorScheme.getMainColor() + "&l* " + colorScheme.getMainColor() + "Permission: &f" + command.getPermission());
            }
            if (!command.getDescription().equalsIgnoreCase("none")) {
                message.append(colorScheme.getMainColor() + "&l* " + colorScheme.getMainColor() + "Description: &f" + command.getDescription());
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
            OChatMessage message = new OChatMessage();
            message.append(colorScheme.getSecondColor() + "&l---====" + colorScheme.getMainColor() + " " + StringUtils.capitalize(command.getLabel()) + " Help");

            //Format Builder
            if (hasOptional && hasRequired)
                message.append(colorScheme.getSecondColor() + "   <> - Required, [] - Optional");

            else if (hasRequired)
                message.append(colorScheme.getSecondColor() + "   <> - Required");

            else if (hasOptional)
                message.append(colorScheme.getSecondColor() + "   [] - Optional");

            message.append(" ");

            //Check if main command has stuff :D
            if(command.getListener() != null) {
                ChatLine line = new ChatLine();

                String allParents = command.getLabelWithParents();
                LineContent labelContent = new LineContent(colorScheme.getMainColor() + "/" + reverseLabel(allParents.substring(0, allParents.length() - 1)));
                if (!command.getDescription().equalsIgnoreCase("None"))
                    labelContent.hover().add(colorScheme.getMainColor() + command.getDescription());
                line.append(labelContent);

                buildArgs(command.getArgumentMap().values(), line);
                if (!command.getDescription().equalsIgnoreCase("None"))
                    line.append(colorScheme.getMainColor() + " - " + command.getDescription());

                message.append(line);
            }

            for (OCommand subCommand : command.getSubCommands().values()) {

                //Check if the command is hidden
                if (subCommand.isSecret())
                    continue;

                //Check for permission (if sender has the permission to use this command)
                if (subCommand.getPermission().equalsIgnoreCase("none") && !sender.hasPermission(subCommand.getPermission()))
                    continue;

                ChatLine line = new ChatLine();
                line.append(colorScheme.getMainColor() + "/" + reverseLabel(command.getLabelWithParents()));

                LineContent labelContent = new LineContent(colorScheme.getMainColor() + subCommand.getLabel());
                if (!subCommand.getDescription().equalsIgnoreCase("None"))
                    labelContent.hover().add(colorScheme.getMainColor() + subCommand.getDescription());
                line.append(labelContent);

                buildArgs(subCommand.getArgumentMap().values(), line);
                if (!subCommand.getDescription().equalsIgnoreCase("None"))
                    line.append(colorScheme.getMainColor() + " - " + subCommand.getDescription());

                message.append(line);

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
            buffer.append(append).append(" ");
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

    private void buildArgs(Collection<CommandArgument> args, ChatLine line) {
        //Format = <required> [optional]
        if (args.stream().anyMatch(CommandArgument::isRequired)) {
            args.stream().filter(CommandArgument::isRequired).forEach(arg -> {
                line.append(" <");
                LineContent content = new LineContent("&f" + arg.getIdentity()).
                        hover().add(colorScheme.getMainColor() + arg.getDescription()).parent();
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
                        hover().add(colorScheme.getSecondColor() + arg.getDescription()).parent();
                line.append(content);
                line.append(colorScheme.getSecondColor() + "]");

            });
            line.append(colorScheme.getMarkupColor());
        }
    }
}
