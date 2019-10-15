package com.oop.orangeengine.command.arg.arguments;

import com.oop.orangeengine.command.arg.CommandArgument;
import com.oop.orangeengine.main.util.data.pair.OPair;

public class StringArg extends CommandArgument<String> {

    public StringArg() {
        setDescription("A text");
        setIdentity("text");
        setMapper(text -> new OPair<>(text, null));
    }

}
