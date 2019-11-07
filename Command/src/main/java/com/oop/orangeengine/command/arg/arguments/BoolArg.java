package com.oop.orangeengine.command.arg.arguments;

import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

public class BoolArg extends CommandArgument<Boolean> {

    public BoolArg() {

        setIdentity("boolean");
        setDescription("A boolean (true or false)");
        setMapper(input -> {
            if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false"))
                return new OPair<>(Boolean.parseBoolean(input), "");
            else
                return new OPair<>(null, "Failed to parse " + input + " as a boolean!");
        });
    }

}
