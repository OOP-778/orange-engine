package com.oop.orangeengine.reflection.resolve.resolvers;

import com.oop.orangeengine.main.util.OQueue;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.reflection.OClass;
import lombok.Data;

import java.util.Optional;
import java.util.function.Consumer;

@Data
public abstract class AResolvers<R> extends AResolver<Object> {

    private OQueue<R> resolved = new OQueue<>();
    private Consumer<Throwable> ifFailed;

    public AResolvers(OClass holder) {
        super(holder);
    }

    public OptionalConsumer<R> nextResolved() {
        if(resolved.isEmpty())
            throw new IllegalStateException("Failed to find find next value of Queue, queue is empty!");

        else
            return OptionalConsumer.of(Optional.ofNullable(resolved.poll()));
    }
}
