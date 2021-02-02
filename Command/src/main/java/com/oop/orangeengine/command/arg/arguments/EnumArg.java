package com.oop.orangeengine.command.arg.arguments;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EnumArg<T extends Enum> extends CommandArgument<T> {
    private List<String> values;

    public EnumArg(Class<T> clazz) {
        values = Arrays.stream(clazz.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
        setIdentity("enum");
        setDescription("An enum");
        setMapper(input -> {
            Optional<T> ifPresent = Enums.getIfPresent(clazz, input.toUpperCase(Locale.ROOT));
            return new OPair<>(ifPresent.or((T)null), "Failed to find " + getIdentity() + " by '" + input + "'");
        });
    }

    @Override
    public void onAdd(OCommand command) {
        command.nextTabComplete(((previousResult, args) -> values));
    }
}
