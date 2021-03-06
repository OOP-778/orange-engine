package com.oop.orangeengine.command;

import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.command.req.RequirementMapper;
import com.oop.orangeengine.command.scheme.DefaultScheme;
import com.oop.orangeengine.command.scheme.Scheme;
import com.oop.orangeengine.command.scheme.SchemeHolder;
import com.oop.orangeengine.main.Helper;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.message.impl.chat.ChatLine;
import com.oop.orangeengine.message.impl.chat.InsertableList;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.oop.orangeengine.main.Engine.getEngine;
import static com.oop.orangeengine.main.Helper.color;

public class CommandController {

    @Setter
    private SchemeHolder schemeHolder;

    @Getter
    private Map<String, OCommand> registeredCommands = new ConcurrentHashMap<>();

    private CommandMap commandMap;

    public CommandController() {
        this(null);
    }

    public CommandController(SchemeHolder holder) {
        this.schemeHolder = holder;
        if (schemeHolder == null)
            schemeHolder = DefaultScheme.getDefaultHolder();

        getEngine().getOwning().onDisable(this::unregisterAll);
        try {

            Field cMap = SimplePluginManager.class.getDeclaredField("commandMap");
            cMap.setAccessible(true);
            commandMap = (CommandMap) cMap.get(Bukkit.getPluginManager());

        } catch (Throwable thrw) {
            throw new IllegalStateException("Failed to initialize CommandMap", thrw);
        }
    }

    public void register(OCommand command) {
        Command bukkitCommand = new WrappedBukkitCommand(command.getLabel(), command.getDescription() == null ? "" : command.getDescription(), "none", new ArrayList<>(command.getAliases())) {
            @Override
            public boolean execute(CommandSender sender, String cmdName, String[] args) {
                try {
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
                                sender.sendMessage(color("&cSub Command by name " + secondArg + " not found!"));
                                handleProperUsage(command, sender);

                            } else
                                handleCommand(args, sender, command);
                        }

                    }
                } catch (Throwable throwable) {
                    getEngine().getLogger().error(throwable);
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

        command.setRegisteredCommand(bukkitCommand);
        registeredCommands.put(command.getLabel().toLowerCase(), command);
        commandMap.register(getEngine().getOwning().getName(), bukkitCommand);
    }

    public Collection<String> getSubCommandsFor(OCommand command, CommandSender sender) {
        return command.getSubCommands().values()
                .stream()
                .filter(subCommand -> !subCommand.isSecret())
                .filter(subCommand -> subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission()))
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

        ResultCache cache = new ResultCache();
        if (completion.isEmpty()) {
            List<CommandArgument> arguments = new ArrayList<>(command.getArgumentMap().values());
            for (int i = 0; i < args.length - 1; i++) {
                if ((arguments.size() - 1) <= i)
                    break;

                CommandArgument commandArgument = arguments.get(i);
                OPair<Object, String> value = commandArgument.map(args[i], cache);
                if (value.getKey() != null)
                    cache.getObjects().add(value.getKey());
            }

            TabCompletion tabCompletion = command.getTabComplete().get(args.length);
            if (tabCompletion != null)
                completion.addAll(tabCompletion.handleTabCompletion(cache, args));
        }

        if (args.length >= 1) {
            String lastArg = args[args.length - 1].toLowerCase(Locale.ROOT);
            if (lastArg.trim().length() > 0)
                completion.removeIf(name -> name == null || !name.toLowerCase().startsWith(lastArg));
        }

        completion.removeIf(Objects::isNull);
        return completion;
    }


    private void handleCommand(String[] args, CommandSender sender, OCommand command) {
        // Check for permission
        if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
            OChatMessage message = new OChatMessage(schemeHolder.getScheme(command, "no permission").getScheme());
            message.replace(getPlaceholdersForCommand(command));
            message.send(sender);
            return;
        }

        // Check for ableToExecute
        int found = 0;
        for (Class executerClass : command.getAbleToExecute())
            if (executerClass.isAssignableFrom(sender.getClass())) found++;

        if (found == 0) {
            sender.sendMessage(color(command.getNotAbleToExecuteMessage().replace("%sender%", sender instanceof Player ? "Console" : "Player")));
            return;
        }

        //Check for sender requirements
        for (RequirementMapper requirementMapper : command.getRequirementSet()) {

            OPair<Boolean, String> mapped = requirementMapper.accepts(sender);
            if (!mapped.getFirst()) {

                sender.sendMessage(color(mapped.getSecond()));
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

            ResultCache cache = new ResultCache();
            for (CommandArgument arg : commandArguments) {
                if (argsCopy.length == 0) {
                    if (!arg.isRequired()) continue;
                    handleProperUsage(command, sender);
                    return;
                }

                String stringValue = argsCopy[0];
                try {
                    if (arg.getMapper() != null) {

                        OPair<Object, String> value = arg.map(stringValue, cache);
                        if (value.getFirst() == null) {
                            Scheme error = schemeHolder.getScheme(command, "error");
                            OChatMessage message = new OChatMessage(error.getScheme());
                            message.replace(getPlaceholdersForCommand(command));
                            message.replace("{error_cause}", value.getSecond());
                            message.send(sender);
                            return;

                        } else {
                            arguments.put(arg.getIdentity(), value.getFirst());
                            cache.getObjects().add(value.getFirst());
                            argsCopy = cutArray(argsCopy, 1);
                        }
                    }
                } catch (Throwable throwable) {
                    Scheme error = schemeHolder.getScheme(command, "error");
                    OChatMessage message = new OChatMessage(error.getScheme());
                    message.replace(getPlaceholdersForCommand(command));
                    message.replace("{error_cause}", throwable.getMessage());
                    message.send(sender);
                }

                if (arg.isGrabAllNextArgs()) {
                    StringBuilder current = new StringBuilder((String) arguments.get(arg.getIdentity()));
                    for (String nextArg : argsCopy) {
                        current.append(" ").append(nextArg);
                    }

                    arguments.remove(arg.getIdentity());
                    arguments.put(arg.getIdentity(), current.toString());
                    break;
                }
            }

            execCommand(new WrappedCommand(sender, arguments), command);
        }
    }

    public void unregisterSimilar() {
        unregisterAllSimilar(registeredCommands.values().toArray(new OCommand[0]));
    }

    public void unregisterAllSimilar(OCommand... cmds) {
        try {
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);

            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            knownCommands.entrySet().removeIf(entry -> {
                List<String> all = new ArrayList<>(entry.getValue().getAliases());
                all.add(entry.getValue().getLabel());
                return all.stream().anyMatch(l -> Arrays.stream(cmds).anyMatch(l2 -> l2.getLabel().equalsIgnoreCase(l) || l2.getAliases().stream().anyMatch(l3 -> l3.equalsIgnoreCase(l))));
            });

            for (OCommand cmd : cmds)
                register(cmd);

        } catch (Exception e) {
            throw new IllegalStateException("Could not unregister commands", e);
        }
    }

    public void unregisterAll() {
        try {
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);

            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            knownCommands.entrySet().removeIf(entry -> entry.getValue() instanceof WrappedBukkitCommand);
        } catch (Exception e) {
            throw new IllegalStateException("Could not unregister commands", e);
        }
    }

    private void execCommand(WrappedCommand command, OCommand oopCommand) {
        try {
            oopCommand.getListener().accept(command);
        } catch (Throwable throwable) {
            getEngine().getLogger().error(throwable);
        }
    }

    private String[] cutArray(String[] array, int amount) {
        if (array.length <= amount)
            return new String[0];
        else
            return Arrays.copyOfRange(array, amount, array.length);
    }

    private Map<String, Object> getPlaceholdersForCommand(OCommand command) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("{command_label}", command.getLabel());
        placeholders.put("{command_full_label}", command.getLabelWithParents());
        placeholders.put("{command_description}", command.getDescription() == null ? "none" : command.getDescription());
        placeholders.put("{command_permission}", command.getPermission() == null ? "none" : command.getPermission());

        return placeholders;
    }

    private void handleProperUsage(OCommand command, CommandSender sender) {
        // Check if simple message is required
        if (command.getSubCommands().isEmpty()) {
            OChatMessage message = new OChatMessage();
            Scheme scheme = schemeHolder.getScheme(command, "proper usage");

            for (String line : scheme.getScheme()) {
                if ((line.contains("{command_description}") && command.getDescription() == null) && (line.contains("{command_permission}") && command.getPermission() == null))
                    continue;
                message.append(line);
            }

            scheme.getTemplates().forEach((key, value) -> message.replace("{" + key.replace(" ", "_") + "}", value));
            message.replace(getPlaceholdersForCommand(command));

            boolean hasRequired = command.getArgumentMap().values()
                    .stream()
                    .anyMatch(CommandArgument::isRequired);

            boolean hasOptional = command.getArgumentMap().values()
                    .stream()
                    .anyMatch(argument -> !argument.isRequired());

            if (!hasRequired)
                message.replace("{command_required_args}", "");
            else
                buildArgs(message, "{command_required_args}", scheme.getTemplate("required arg").get(), command.getArgumentMap().values().stream().filter(CommandArgument::isRequired).collect(Collectors.toList()));

            if (!hasOptional)
                message.replace("{command_optional_args}", "");
            else
                buildArgs(message, "{command_optional_args}", scheme.getTemplate("optional arg").get(), command.getArgumentMap().values().stream().filter(argument -> !argument.isRequired()).collect(Collectors.toList()));
            List<TextComponent> components = message.crateTextComponents();

            for (TextComponent component : components)
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(component);
                else
                    sender.sendMessage(Helper.color(component.toPlainText()));

        } else {
            boolean wholeHasRequired = command.getSubCommands().values().stream().
                    map(cmd -> (int) cmd.getArgumentMap().values().stream().filter(CommandArgument::isRequired).count()).
                    anyMatch(c -> c > 0);

            boolean wholeHasOptional = command.getSubCommands().values().stream().
                    map(cmd -> (int) cmd.getArgumentMap().values().stream().filter(a -> !a.isRequired()).count()).
                    anyMatch(c -> c > 0);

            Scheme scheme = schemeHolder.getScheme(command, "sub list");
            OChatMessage message = new OChatMessage();

            // Append scheme to the messsge
            for (String text : scheme.getScheme())
                message.append(text);

            message.replace(getPlaceholdersForCommand(command));

            // Replace {required}, {optional}
            if (wholeHasRequired)
                scheme.getTemplate("required").ifPresent(rm -> message.replace("{required}", rm));
            else
                message.replace("{required}", "");

            if (wholeHasOptional)
                scheme.getTemplate("optional").ifPresent(rm -> message.replace("{optional}", rm));
            else
                message.replace("{optional}", "");

            // Construct sub commands message
            scheme.getTemplate("sub command").ifPresent(sct -> {
                OChatMessage subCommandsMessage = new OChatMessage();
                for (OCommand sc : command.getSubCommands().values()) {
                    if (sc.isSecret() || (sc.getPermission() != null && !sender.hasPermission(sc.getPermission())))
                        continue;

                    int found = 0;
                    for (Class executerClass : command.getAbleToExecute())
                        if (executerClass.isAssignableFrom(sender.getClass())) found++;

                    if (found == 0)
                        continue;

                    OChatMessage scm = sct.clone();
                    boolean hasRequired = sc.getArgumentMap().values()
                            .stream()
                            .anyMatch(CommandArgument::isRequired);

                    boolean hasOptional = sc.getArgumentMap().values()
                            .stream()
                            .anyMatch(argument -> !argument.isRequired());

                    if (!hasRequired)
                        scm.replace("{command_required_args}", "");
                    else
                        buildArgs(scm, "{command_required_args}", scheme.getTemplate("required arg").get(), sc.getArgumentMap().values().stream().filter(CommandArgument::isRequired).collect(Collectors.toList()));

                    if (!hasOptional)
                        scm.replace("{command_optional_args}", "");
                    else
                        buildArgs(scm, "{command_optional_args}", scheme.getTemplate("optional arg").get(), sc.getArgumentMap().values().stream().filter(argument -> !argument.isRequired()).collect(Collectors.toList()));

                    scm.replace(getPlaceholdersForCommand(sc));
                    subCommandsMessage.append(scm);
                }

                int index = message.indexOf(line -> line.raw().contains("{sub_command_template}"));
                message.insert(index, subCommandsMessage, InsertableList.InsertMethod.REPLACE);
            });

            for (TextComponent component : message.crateTextComponents())
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(component);
                else
                    sender.sendMessage(Helper.color(component.toPlainText()));
        }
    }

    public Map<String, Object> getPlaceholdersForArgument(CommandArgument argument) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("{arg_identifier}", argument.getIdentity());
        placeholders.put("{arg_description}", argument.getDescription() == null ? "none" : argument.getDescription());
        return placeholders;
    }

    public void buildArgs(OChatMessage parent, String pkey, OChatMessage argTemplate, List<CommandArgument> arguments) {
        ChatLine parentLine = new ChatLine();
        for (CommandArgument argument : arguments) {
            ChatLine line = argTemplate.lineList().get(0).clone();
            line.replace(getPlaceholdersForArgument(argument));

            if (arguments.indexOf(argument) < arguments.size()) {
                parentLine.append(line);
                parentLine.append(" ");
            }
        }

        parent.replace(pkey, parentLine);
    }
}
