package com.oop.orangeengine.command.scheme;

import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.message.impl.chat.LineContent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DefaultScheme {

    private static String mainColor = "&e";
    private static String secondColor = "&6";

    private static Supplier<SchemeHolder> defaultHolderSupplier = () -> {
        SchemeHolder holder = new SchemeHolder()
                // Proper Command Usage
                .addDefault(
                        "proper usage",
                        new Scheme(parse(Arrays.asList("{m}&lUSAGE: {command_usage}", "{s}&l* &7Description: {command_description}", "{s}&l* &7Permission: {command_permission}")))
                                .addTemplate(
                                        "command usage",
                                        new OChatMessage(
                                                new LineContent("{m}/{command_full_label} ")
                                                        .hover()
                                                        .add("{command_description}")
                                                        .parent()
                                                        .suggestion()
                                                        .suggestion("/{command_full_label}")
                                                        .parent(),
                                                new LineContent("{command_required_args}"),
                                                new LineContent("{command_optional_args}")
                                        )
                                )
                                .addTemplate(
                                        "required arg",
                                        new OChatMessage(
                                                new LineContent("{m}<"),
                                                new LineContent("&f{arg_identifier}")
                                                        .hover()
                                                        .add("{m}{arg_description}")
                                                        .parent(),
                                                new LineContent("{m}>")
                                        )
                                )
                                .addTemplate(
                                        "optional arg",
                                        new OChatMessage(
                                                new LineContent("{s}["),
                                                new LineContent("&f{arg_identifier}")
                                                        .hover()
                                                        .add("{s}{arg_description}")
                                                        .parent(),
                                                new LineContent("{s}]")
                                        )
                                )

                )
                .addDefault(
                        "sub list",
                        new Scheme(parse(Arrays.asList("{s}&l----< {m}&l{command_label} &fHelp", "{required} {optional}", " ", "{sub_command_template}")))
                                .addTemplate("optional", new OChatMessage("&7[] - Optional"))
                                .addTemplate("required", new OChatMessage("&7<> - Required"))
                                .addTemplate(
                                        "sub command",
                                        new OChatMessage(
                                                new LineContent("{s}&l- "),
                                                new LineContent("{m}{command_label} ")
                                                        .hover()
                                                        .add("{command_description}")
                                                        .parent()
                                                        .suggestion()
                                                        .suggestion("/{command_full_label}")
                                                        .parent(),
                                                new LineContent("{command_required_args}"),
                                                new LineContent("{command_optional_args}")
                                        )
                                )
                                .addTemplate(
                                        "command usage",
                                        new OChatMessage(
                                                new LineContent("{m}/{command_full_label} ")
                                                        .hover()
                                                        .add("{command_description}")
                                                        .parent()
                                                        .suggestion()
                                                        .suggestion("/{command_full_label}")
                                                        .parent(),
                                                new LineContent("{command_required_args}"),
                                                new LineContent("{command_optional_args}")
                                        )
                                )
                                .addTemplate(
                                        "required arg",
                                        new OChatMessage(
                                                new LineContent("{m}<"),
                                                new LineContent("&f{arg_identifier}")
                                                        .hover()
                                                        .add("{m}{arg_description}")
                                                        .parent(),
                                                new LineContent("{m}>")
                                        )
                                )
                                .addTemplate(
                                        "optional arg",
                                        new OChatMessage(
                                                new LineContent("{s}["),
                                                new LineContent("&f{arg_identifier}")
                                                        .hover()
                                                        .add("{s}{arg_description}")
                                                        .parent(),
                                                new LineContent("{s}]")
                                        )
                                )
                )
                .addDefault(
                        "error",
                        new Scheme(parse(Arrays.asList("&cAn error occurred while executing {command_label} command!", "&c&l* &7Cause: &4{error_cause}")))
                )
                .addDefault(
                        "no permission",
                        new Scheme(parse(Arrays.asList("&cYou don't have the permission to use this command!", "&c&l* &7Permission: {command_permission}")))
                );

        for (Map<String, Scheme> value : holder.getSchemes().values()) {
            for (Scheme scheme : value.values()) {
                for (OChatMessage message : scheme.getTemplates().values()) {
                    message.replace("{m}", mainColor);
                    message.replace("{s}", secondColor);
                }
            }
        }
        return holder;
    };

    private static List<String> parse(List<String> list) {
        return list.stream().map(DefaultScheme::parse).collect(Collectors.toList());
    }

    private static String parse(String text) {
        return text.replace("{m}", mainColor).replace("{s}", secondColor);
    }

    public static SchemeHolder getDefaultHolder() {
        return defaultHolderSupplier.get();
    }

    public static void setMainColor(String mainColor) {
        DefaultScheme.mainColor = mainColor;
    }

    public static void setSecondColor(String secondColor) {
        DefaultScheme.secondColor = secondColor;
    }
}
