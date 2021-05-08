package com.oop.orangeengine.command;

import com.oop.orangeengine.file.OFile;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import com.oop.orangeengine.yaml.Config;
import com.oop.orangeengine.yaml.ConfigSection;

import java.util.*;
import java.util.function.Consumer;

import static com.oop.orangeengine.main.Engine.getEngine;

public class CommandRemapper {
    public Map<String, Consumer<OCommand>> remappers = new HashMap<>();
    private Config config;

    public CommandRemapper() {
        EnginePlugin owning = getEngine().getOwning();
        if (!owning.getDataFolder().exists())
            owning.getDataFolder().mkdirs();

        OFile file = new OFile(owning.getDataFolder(), "commands.yml").createIfNotExists();
        config = new Config(file);
    }

    public void updateConfig(OCommand... commands) {
        // Remove old entries
        for (String s : new HashSet<>(config.getSections().keySet())) {
            if (Arrays.stream(commands).map(OCommand::getLabel).noneMatch(label -> label.contentEquals(s)))
                config.getSections().remove(s);
        }

        for (OCommand command : commands) {
            ConfigSection section = config.createSection(command.getLabel());
            updateCommand(section, command);
        }

        config.save();
    }

    public void remap(OCommand... commands) {
        Map<String, ConfigSection> hierarchySections = config.getHierarchySections();
        hierarchySections.forEach((path, section) -> {
            if (path.endsWith("sub commands")) return;
            path = path.replace("sub commands.", "");

            remappers.put(path, command -> {
                String label = section.getAs("label");
                String description = section.getAs("description");
                String permission = section.getAs("permission");
                Collection<String> alias = section.getAs("alias");

                command.getAliases().clear();
                command.alias(alias.toArray(new String[0]));
                command.label(label);
                command.permission(permission.equalsIgnoreCase("None") ? null : permission);
                command.description(description);
            });
        });

        Map<String, OCommand> commandsByPaths = new HashMap<>();
        for (OCommand command : commands)
            commandsByPaths.putAll(buildCommandPaths(command));

        // Remap commands
        commandsByPaths.forEach((path, command) -> {
            Consumer<OCommand> oCommandConsumer = remappers.get(path);
            if (oCommandConsumer != null)
                oCommandConsumer.accept(command);
        });
    }

    public Map<String, OCommand> buildCommandPaths(OCommand command) {
        Map<String, OCommand> paths = new HashMap<>();
        paths.put(command.getLabelWithParents("."), command);

        for (OCommand value : command.getSubCommands().values())
            paths.putAll(buildCommandPaths(value));

        return paths;
    }

    private void updateCommand(ConfigSection section, OCommand command) {
        if (!section.isValuePresent("label"))
            section.set("label", section.getKey());

        if (!section.isValuePresent("description"))
            section.set("description", command.getDescription() == null ? "None" : command.getDescription());

        if (!section.isValuePresent("permission"))
            section.set("permission", command.getPermission() == null ? "None" : command.getPermission());

        if (!section.isValuePresent("alias"))
            section.set("alias", command.getAliases());

        if (!command.getSubCommands().values().isEmpty()) {
            ConfigSection subCommandsSection = section.createSection("sub commands");
            for (OCommand value : command.getSubCommands().values()) {
                ConfigSection subCommandSection = subCommandsSection.createSection(value.getLabel());
                updateCommand(subCommandSection, value);
            }
        }
    }
}
