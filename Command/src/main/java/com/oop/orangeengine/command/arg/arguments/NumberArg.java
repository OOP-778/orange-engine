package com.oop.orangeengine.command.arg.arguments;

import com.google.common.collect.ArrayListMultimap;
import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

import java.text.NumberFormat;

public class NumberArg extends CommandArgument<Number> {
    public NumberArg() {
        setDescription("A number");
        setIdentity("number");
        setMapper(in -> {
            ArrayListMultimap.create();
            try {
                return new OPair<>(NumberFormat.getInstance().parse(in), "");
            } catch (Throwable thrw) {
                return new OPair<>(null, "Failed to parse " + in + " into number!");
            }
        });
    }
}
