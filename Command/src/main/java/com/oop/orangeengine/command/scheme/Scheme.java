package com.oop.orangeengine.command.scheme;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.message.YamlMessage;
import com.oop.orangeengine.message.impl.OChatMessage;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public class Scheme {

    private final List<String> scheme;
    private final Map<String, OChatMessage> templates;

    protected Scheme(List<String> scheme) {
        this.scheme = scheme;
        this.templates = new HashMap<>();
    }

    public Scheme addTemplate(String id, OChatMessage template) {
        templates.put(id, template);
        return this;
    }

    public static Scheme of(ConfigSection section) {
        return new Scheme(
                section.getAs("scheme"),
                section
                        .getSection("templates")
                        .map(templateSection -> Stream.concat(
                                templateSection.getSections().values().stream().map(ts -> new OPair<>(ts.getKey().toLowerCase(), YamlMessage.Chat.load(ts))),
                                templateSection.getValues().values().stream().map(tv -> new OPair<>(tv.getKey().toLowerCase(), YamlMessage.Chat.load(tv)))
                        ))
                        .map(stream -> stream.collect(Collectors.toMap(OPair::getKey, OPair::getValue)))
                        .orElse(new HashMap<>())
        );
    }

    public Optional<OChatMessage> getTemplate(String id) {
        return Optional.ofNullable(templates.get(id.toLowerCase()));
    }
}
