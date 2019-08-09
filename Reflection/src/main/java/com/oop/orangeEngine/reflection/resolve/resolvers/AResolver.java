package com.oop.orangeEngine.reflection.resolve.resolvers;

import com.oop.orangeEngine.reflection.OClass;
import lombok.Data;

import java.util.Optional;
import java.util.function.Consumer;

@Data
public abstract class AResolver<T> {

    private OClass holder;
    private Consumer<Throwable> ifFailed;

    public AResolver(OClass holder) {
        this.holder = holder;
    }

    public Class<?> getJavaHolder() {
        return holder.getJavaClass();
    }

    public void handleException(Throwable throwable) {

        try {
            throw throwable;
        } catch (Throwable e) {

            if(getIfFailed() != null)
                getIfFailed().accept(e);

            else
                e.printStackTrace();

        }

    }

    public Optional<T> resolve(){
        return null;
    }

}
