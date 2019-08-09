package com.oop.orangeEngine.command.arg.arguments;

import com.oop.orangeEngine.command.arg.CommandArgument;
import com.oop.orangeEngine.main.util.pair.OPair;

public class IntArg extends CommandArgument<Integer> {

    public IntArg() {
        setDescription("Integer");
        setIdentity("int");
        setMapper(input -> {

            boolean isInteger = false;
            try {
                Integer.parseInt(input);
                isInteger = true;
            } catch (Exception ignored) {}

            return new OPair<>(
                    isInteger ? Integer.parseInt(input) : null,
                    isInteger ? "" : "Failed to parse " + input + " as an Integer!"
            );
        });
    }

}
