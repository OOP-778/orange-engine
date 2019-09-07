package com.oop.orangeengine.command.arg;

import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;

@Getter
public abstract class CommandArgument<T> {

    //Argument parser / Will return as String if not defined
    private ArgumentMapper mapper;

    //Will be used on when list of the commands are sent / On proper command usage / When getting the arguments
    private String identifier = "";

    //Will be used as hover text in list of the commands are sent / On proper command usage
    private String description = "";

    //If it's required command won't run if not found, else it will run.
    private boolean required = false;

    public CommandArgument<T> setMapper(ArgumentMapper<T> mapper) {
        this.mapper = mapper;
        return this;
    }

    public CommandArgument<T> setIdentity(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public CommandArgument<T> setDescription(String description) {
        this.description = description;
        return this;
    }

    public CommandArgument<T> setIsRequired(boolean required) {
        this.required = required;
        return this;
    }

    public void onAdd(OCommand command) {
    }

    public interface ArgumentMapper<T> {

        //Returns Parsed value and if failed to parse value error message.
        OPair<T, String> product(String input);

    }

}
