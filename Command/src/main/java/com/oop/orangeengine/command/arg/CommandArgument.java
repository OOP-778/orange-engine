package com.oop.orangeengine.command.arg;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.oop.orangeengine.command.OCommand;
import com.oop.orangeengine.command.ResultCache;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Getter
@Setter
@Accessors(chain = true)
public abstract class CommandArgument<T> {

    //Argument parser / Will return as String if not defined
    private ArgumentMapper<T> mapper;

    //Will be used on when list of the commands are sent / On proper command usage / When getting the arguments
    private String identity = "";

    //Will be used as hover text in list of the commands are sent / On proper command usage
    private String description = "";

    //If it's required command won't run if not found, else it will run.
    private boolean required = false;

    // It it will grab all the remaining args
    private boolean grabAllNextArgs = false;

    public void onAdd(OCommand command) {}

    public OPair<T, String> map(String in, ResultCache cache) {
        Preconditions.checkArgument(mapper != null, "Mapper cannot be null!");
        if (mapper instanceof AdvancedArgumentMapper)
            return ((AdvancedArgumentMapper<T>) mapper).product(in, cache);
        else
            return mapper.product(in);
    }

    public interface ArgumentMapper<T> {
        // Returns Parsed value and if failed to parse value error message.
        OPair<T, String> product(String input);
    }

    public interface AdvancedArgumentMapper<T> extends ArgumentMapper<T> {
        @Override
        default OPair<T, String> product(String input) {
            return null;
        }

        OPair<T, String> product(String input, ResultCache cache);
    }
}
