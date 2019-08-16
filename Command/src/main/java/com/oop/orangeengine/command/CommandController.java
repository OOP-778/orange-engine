package com.oop.orangeengine.command;

import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.command.req.RequirementMapper;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.pair.OPair;
import com.oop.orangeengine.message.OMessage;
import com.oop.orangeengine.message.line.LineContent;
import com.oop.orangeengine.message.line.MessageLine;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class CommandController {

    private static CommandMap commandMap;
    private static boolean init = false;

    private ColorScheme colorScheme;
    private JavaPlugin plugin;

    public CommandController(JavaPlugin plugin) {

        this.plugin = plugin;
        this.colorScheme = new ColorScheme();
        if (!init) {
            try {

                Field cMap = SimplePluginManager.class.getDeclaredField("commandMap");
                cMap.setAccessible(true);
                commandMap = (CommandMap) cMap.get(Bukkit.getPluginManager());
                init = true;

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public void register(OCommand command) {

        commandMap.register(plugin.getName(), new Command(command.getLabel(), command.getDescription(), "none", new ArrayList<>(command.getAliases())) {
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
                    OCommand subCommand = command.getSubCommands().get(secondArg);
                    if (subCommand != null)
                        handleCommand(cutArray(args, 1), sender, subCommand);

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
                    command.getSubCommands().forEach((n, c) -> {
                        if (c.isSecret())
                            return;

                        if (!c.getPermission().equalsIgnoreCase("NONE") && !sender.hasPermission(c.getPermission()))
                            return;

                        completion.add(c.getLabel());

                    });
                } else {

                    if (command.isSubCommand(args[0]))
                        completion.addAll(handleTabComplete(command.subCommand(args[0]), cutArray(args, 1)));

                }

                if(completion.isEmpty())
                    completion.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));

                return completion;
            }
        });

    }

    public List<String> handleTabComplete(OCommand command, String[] args) {

        List<String> completion = new ArrayList<>();
        if (command.getTabComplete().containsKey(args.length))
            completion.addAll(command.getTabComplete().get(args.length).handleTabCompletion(args));

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
            if(executerClass == sender.getClass()) found++;

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
            for (CommandArgument arg : command.getArgumentMap().values()) {

                if (argsCopy.length == 0) {

                    if (!arg.isRequired()) continue;
                    handleProperUsage(command, sender);
                    return;

                }

                String stringValue = argsCopy[0];
                if (arg.getMapper() != null) {

                    OPair<Object, String> value = arg.getMapper().product(stringValue);
                    if (value.getFirst() == null) {

                        sender.sendMessage(colorize("&c&l* &7Error: &c" + value.getFirst()));
                        handleProperUsage(command, sender);
                        return;

                    } else {

                        arguments.put(arg.getIdentifier(), value.getSecond());
                        argsCopy = cutArray(argsCopy, 1);

                    }
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

            Helper.print("HAS PARENT: " + command.getParent() != null);
            String allParents = command.getLabelWithParents();

            line.append(colorScheme.getMainColor() + "Usage: /" + reverseLabel(allParents.substring(0, allParents.length() - 1)));

            LineContent labelContent = new LineContent(colorScheme.getMainColor() + command.getLabel());
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
                if (!command.getDescription().equalsIgnoreCase(""))
                    labelContent.hoverText(colorScheme.getMainColor() + command.getDescription());
                line.append(labelContent);

                buildArgs(command.getArgumentMap().values(), line);
                if (!command.getDescription().equalsIgnoreCase(""))
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
                if (!subCommand.getDescription().equalsIgnoreCase(""))
                    labelContent.hoverText(colorScheme.getMainColor() + subCommand.getDescription());
                line.append(labelContent);

                buildArgs(subCommand.getArgumentMap().values(), line);
                if (!subCommand.getDescription().equalsIgnoreCase(""))
                    line.append(colorScheme.getMainColor() + " - " + subCommand.getDescription());

                message.appendLine(line);

            }
            message.appendLine(" ");
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

            line.append(colorScheme.getMainColor());
            args.stream().filter(CommandArgument::isRequired).forEach(arg -> {

                line.append("<");
                LineContent content = new LineContent("&f" + arg.getIdentifier()).
                        hoverText(colorScheme.getMainColor() + arg.getDescription());
                line.append(content);
                line.append(">");

            });
            line.append(colorScheme.getMainColor());

        }

        if (args.stream().anyMatch(a -> !a.isRequired())) {

            line.append(colorScheme.getSecondColor());
            args.stream().filter(a -> !a.isRequired()).forEach(arg -> {

                line.append("[");
                LineContent content = new LineContent("&f" + arg.getIdentifier()).
                        hoverText(colorScheme.getSecondColor() + arg.getDescription());
                line.append(content);
                line.append("]");

            });
            line.append(colorScheme.getMarkupColor());

        }

    }

}
