package com.oop.orangeengine.command.arg;

import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public abstract class CommandArgument<T> {

    //Argument parser / Will return as String if not defined
    private ArgumentMapper mapper;

    //Will be used on when list of the commands are sent / On proper command usage / When getting the arguments
    private String identity = "";

    //Will be used as hover text in list of the commands are sent / On proper command usage
    private String description = "";

    //If it's required command won't run if not found, else it will run.
    private boolean required = false;

    // It it will grab all the remaining args
    private boolean grabAllNextArgs = false;

    public void onAdd(OCommand command) {
    }

    public interface ArgumentMapper<T> {

        //Returns Parsed value and if failed to parse value error message.
        OPair<T, String> product(String input);

    }

}
